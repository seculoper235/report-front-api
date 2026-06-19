# 운영 인프라 가이드 (report 프로젝트)

개인 운영용. **3개 노드**로 분리한다 — front-api / admin-api / 데이터 노드(Postgres+Redis).
모든 노드는 AWS Lightsail(Amazon Linux 2023)이며 **Tailscale tailnet**으로만 서로/클라이언트와 통신한다.
API는 AWS S3를 사용한다.

```
[report_application(iPhone)] ──Tailscale──> [front-api 호스트 :8080]  ─┐
[report-admin-app(iPhone)]  ──Tailscale──> [admin-api 호스트 :8080]  ─┤
                                                                       ├─Tailscale─> [데이터 노드]
                                            [GitHub Actions]──rsync+ssh─┘             Postgres(5432)
                                             (각 repo가 자기 호스트로 배포)             Redis(6379)
                                                  │                                   ↑ Tailscale IP에만 바인딩
                                            [AWS S3] report-product-bucket / report-gift-bucket
```

- front-api / admin-api 는 **같은 데이터**(상품·기프티콘·회원·포인트)를 다루므로 **DB/Redis를 공유**한다.
  단 데이터는 두 API 호스트 어디에도 두지 않고 **전용 데이터 노드**에 둔다.
- DB/Redis는 데이터 노드의 **Tailscale IP에만 바인딩** → tailnet에서만 접근, 공개 노출 없음. Redis는 비밀번호 사용.
- 배포: 각 API repo의 `master` push → GitHub Actions가 러너를 tailnet에 합류시켜 자기 호스트로 rsync→`compose up`.

---

## 1. GitHub Environment (`production`) — front-api / admin-api repo 각각 설정

대부분 동일하나 **`LIGHTSAIL_HOST` 만 repo별로 자기 API 호스트의 Tailscale IP**로 다르게 넣는다.

### Secrets (민감)
| 이름 | 값/출처 |
|------|---------|
| `SSH_PRIVATE_KEY` | 배포 개인키 `~/report-deploy-key` 전체 (모든 호스트 authorized_keys에 등록) |
| `TS_OAUTH_CLIENT_ID` / `TS_OAUTH_SECRET` | Tailscale OAuth 클라이언트 |
| `DB_PASSWORD` | Postgres 비밀번호 (데이터 노드와 동일) |
| `REDIS_PASSWORD` | Redis 비밀번호 (데이터 노드와 동일) |
| `JWT_SECRET` | 32바이트+ 랜덤 (front/admin 동일) |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | S3용 IAM 사용자 키 |

### Variables (비민감)
| 이름 | front-api repo | admin-api repo |
|------|----------------|----------------|
| `LIGHTSAIL_HOST` | **front 호스트** Tailscale IP | **admin 호스트** Tailscale IP |
| `DB_HOST` / `REDIS_HOST` | 데이터 노드 Tailscale IP | 데이터 노드 Tailscale IP (동일) |
| `SSH_USER` | `ec2-user` | `ec2-user` |
| `DB_USER` | `devteller` | `devteller` |
| `AWS_REGION` | `ap-northeast-2` | `ap-northeast-2` |
| `AWS_S3_PUBLIC_BUCKET` | `report-product-bucket` | `report-product-bucket` |
| `AWS_S3_PRIVATE_BUCKET` | `report-gift-bucket` | `report-gift-bucket` |
| `JAVA_OPTS` (1GB 번들 권장) | `-Xmx384m -XX:MaxMetaspaceSize=192m` | 동일 |

> GitHub 계정 billing 잠금 시 워크플로가 실행되지 않으므로 결제 상태 먼저 확인.
> 이미지는 GitHub Actions가 빌드해 **GHCR**(`ghcr.io/seculoper235/report-{front,admin}-api`)에 push하고,
> 서버는 그것을 **pull만** 한다(인스턴스 빌드 없음). GHCR pull 인증은 배포 잡의 `GITHUB_TOKEN`으로 처리된다.

---

## 2. 각 노드 공통 1회 세팅 (SSH, ec2-user)

front-api / admin-api / 데이터 노드 **모두** 아래를 수행한다.
서버는 빌드를 하지 않고 GHCR 이미지를 pull만 하므로 **buildx는 불필요**하다.

```bash
echo "<report-deploy-key.pub>" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys

# Docker
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user            # 재로그인

# docker compose 플러그인
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 스왑 2GB (1GB 번들 권장 — pull/기동 중 메모리 여유 확보)
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Tailscale (URL 인증은 로컬 브라우저)
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up                            # → 각 노드의 100.x.x.x 확보
```

콘솔(웹):
- Lightsail 방화벽: **22(SSH)만** 인터넷 허용. API의 8080, 데이터의 5432/6379 는 **인터넷에 열지 않음**(Tailscale 전용).
- Tailscale 콘솔: 각 노드 **key expiry 비활성화**.

---

## 3. 데이터 노드 기동 (Postgres + Redis)

데이터 노드는 GitHub 자동배포 대상이 아니라 직접 띄운다(거의 안 바뀜).
private 레포 + AL2023에 git 미설치이므로 **clone 대신 compose 파일 2개만 로컬에서 scp**한다.

```bash
# (로컬 Mac에서) 데이터 노드로 파일 전송
scp -i ~/report-deploy-key \
  ~/IdeaProjects/report-front-api/docker/docker-compose.data.yml \
  ~/IdeaProjects/report-front-api/docker/.env.data.example \
  ec2-user@<DATA_IP>:~/

# (데이터 노드에서)
tailscale ip -4                              # DATA_BIND_IP / 각 API의 DB_HOST,REDIS_HOST 값
cp .env.data.example .env.data && vi .env.data   # DATA_BIND_IP/DB_USER/DB_PASSWORD/REDIS_PASSWORD 채우기
docker compose -f docker-compose.data.yml --env-file .env.data up -d
```

- ⚠️ **모든 compose 명령에 `--env-file .env.data` 필수.** compose는 `.env`만 자동 로드하므로
  빠뜨리면 변수가 빈 값이 되어 Postgres 기동 실패 + 포트가 `0.0.0.0`(공개)로 열린다.
- `DATA_BIND_IP` = 이 노드의 Tailscale IP. Postgres/Redis가 그 주소에만 바인딩된다.
- `DB_USER/DB_PASSWORD/REDIS_PASSWORD` 는 front/admin 의 `.env.prod` 와 **동일 값**.

검증:
```bash
docker compose -f docker-compose.data.yml --env-file .env.data ps
# daily-db: Up (healthy), daily-redis: Up
# PORTS 가 <DATA_IP>:5432->5432/tcp, <DATA_IP>:6379->6379/tcp 면 정상 (0.0.0.0 이면 env-file 누락)
docker exec report-data-daily-db-1 pg_isready -U devteller -d daily          # accepting connections
docker exec report-data-daily-redis-1 redis-cli -a "<REDIS_PASSWORD>" ping   # PONG
```

---

## 4. API 배포 (front-api / admin-api)

각 repo `master` push → GitHub Actions 자동 배포. 수동은 Actions → Run workflow.
흐름: **Actions에서 이미지 빌드 → GHCR push → 서버에 compose/.env 전달 → 서버가 pull & up**(인스턴스 빌드 없음).
- front-api: 이미지 `ghcr.io/seculoper235/report-front-api`, `~/report-front-api/docker/` 로 compose 전달 (8080)
- admin-api: 이미지 `ghcr.io/seculoper235/report-admin-api`, `~/report-admin-api/docker/` 로 compose 전달 (8080)
- 둘 다 `.env.prod`의 `DB_HOST/REDIS_HOST`(데이터 노드 IP)로 접속. JWT/DB/Redis 자격증명은 동일 값.
- 1GB 번들: `JAVA_OPTS` 변수로 힙 제한(`-Xmx384m -XX:MaxMetaspaceSize=192m`) + 스왑(2장)으로 안정화.

검증:
```bash
curl http://<front 호스트 Tailscale IP>:8080/actuator/health
curl http://<admin 호스트 Tailscale IP>:8080/actuator/health
```

아이폰 앱 배포는 6장 참고.

---

## 5. 로컬·운영 기타

### 키 & 클라이언트
- `ssh-keygen -t ed25519 -f ~/report-deploy-key -N ""` (개인키→Secret, 공개키→모든 노드)
- Mac·iPhone에 Tailscale 로그인(같은 계정), OAuth 클라이언트 + ACL `tag:ci`

### AWS
- S3 버킷 2개(`report-product-bucket` 공개읽기, `report-gift-bucket` 차단) + IAM 사용자(`docker/s3-iam-policy.json`)

### IntelliJ DB 접속
- DB/Redis가 **데이터 노드 Tailscale IP에 바인딩**돼 있어 Mac이 tailnet에 있으면 **SSH 터널 없이 직접 접속**:
  Host `<DATA_IP>`, Port `5432`(Postgres, user `devteller`) / `6379`(Redis, `REDIS_PASSWORD` 필요)

### 관리자 계정
- `signup`(USER) → `UPDATE rpt_user SET role='ADMIN' WHERE email=...`(IntelliJ) → 재로그인

---

## 6. 아이폰 앱 배포 (report_application / report-admin-app)

물리 아이폰에 설치한다. Mac + Xcode 필요. 앱이 tailnet의 API에 붙어야 하므로 **아이폰에 Tailscale 설치·로그인(같은 계정)** 필수.

```bash
# 1) 아이폰을 USB로 연결 → "이 컴퓨터를 신뢰" → 기기 인식 확인
flutter devices

# 2) 서명 1회 설정: ios/Runner.xcworkspace 를 Xcode로 열고
#    Runner ▸ Signing & Capabilities ▸ Automatically manage signing ✓,
#    Team = 본인 Apple ID, Bundle Identifier = 고유값(예: com.<나>.report)

# 3) 실행 (API_BASE_URL 에 해당 노드의 Tailscale IP 주입)
cd ~/IdeaProjects/report_application
flutter run --release -d <iphone> --dart-define=API_BASE_URL=http://<FRONT_IP>:8080

cd ~/IdeaProjects/report-admin-app
flutter run --release -d <iphone> --dart-define=API_BASE_URL=http://<ADMIN_IP>:8080
```

- 첫 설치 후 아이폰: **설정 ▸ 일반 ▸ VPN 및 기기 관리**에서 개발자 앱 **신뢰**.
- **무료 Apple ID**: 인증서 7일 만료 → 7일마다 재설치. 번들 ID 개수 제한. 1년 유효는 개발자 계정($99/년).
- HTTP 평문 접속은 두 앱 모두 `Info.plist`에 ATS 예외가 들어가 있어 동작한다.
- 케이블 없이 쓰려면 Xcode에서 무선 디버깅을 켜거나, 설치 후엔 케이블 없이도 앱은 단독 실행된다(같은 tailnet이면 API 접속됨).

---

## 노드 요약
| 노드 | 실행 | 포트(바인딩) | 자동배포 |
|------|------|--------------|----------|
| 데이터 노드 | `docker-compose.data.yml` | 5432·6379 (Tailscale IP에만) | ✗ (수동) |
| front-api | `docker-compose.prod.yml` | 8080 | ✓ (front repo) |
| admin-api | `docker-compose.prod.yml` | 8080 | ✓ (admin repo) |
