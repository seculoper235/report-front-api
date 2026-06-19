# 운영 인프라 가이드 (report 프로젝트)

개인 운영용. 아이폰 앱(report_application / report-admin-app)이 클라우드 VM(AWS Lightsail, Amazon Linux 2023)
위의 API에 **Tailscale**로 접속하고, API는 Docker로 띄운 Postgres/Redis와 AWS S3를 사용한다.

```
[아이폰/Mac/iPhone] ──Tailscale(tailnet)──> [Lightsail: 100.120.208.61]
                                              ├─ report-front-api (8080)
                                              ├─ report-admin-api (8090, 선택)
                                              ├─ Postgres (daily-db, 127.0.0.1:5432)
                                              └─ Redis    (daily-redis, 127.0.0.1:6379)
                                                     │
[GitHub Actions] ──rsync+ssh(tailnet)──> 배포        │ 인터넷
                                              [AWS S3] report-product-bucket / report-gift-bucket
```

배포는 `master` push 시 GitHub Actions가 러너를 tailnet에 합류시켜 rsync→`docker compose up -d --build`로 수행.

---

## 1. GitHub Environment (`production`)

저장소 → Settings → Environments → `production`

### Secrets (민감)
| 이름 | 값/출처 |
|------|---------|
| `SSH_PRIVATE_KEY` | 배포 개인키 `~/report-deploy-key` 전체 |
| `TS_OAUTH_CLIENT_ID` / `TS_OAUTH_SECRET` | Tailscale OAuth 클라이언트 |
| `DB_PASSWORD` | Postgres 비밀번호 |
| `JWT_SECRET` | 32바이트+ 랜덤 |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | S3용 IAM 사용자 키 |

### Variables (비민감)
| 이름 | 값 |
|------|-----|
| `LIGHTSAIL_HOST` | `100.120.208.61` (Tailscale IP) |
| `SSH_USER` | `ec2-user` |
| `DB_USER` | `devteller` |
| `AWS_REGION` | `ap-northeast-2` |
| `AWS_S3_PUBLIC_BUCKET` | `report-product-bucket` |
| `AWS_S3_PRIVATE_BUCKET` | `report-gift-bucket` |

> GitHub 계정 billing 잠금 시 워크플로가 실행되지 않으므로 결제 상태를 먼저 확인.

---

## 2. EC2(Lightsail)에서 직접 한 작업 (1회 세팅)

최초 접속은 공인 IP + Lightsail 기본키/브라우저 SSH(Tailscale 설치 전). 사용자는 `ec2-user`.

```bash
# 배포 공개키 등록
echo "<report-deploy-key.pub>" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys

# Docker
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user            # 재로그인

# docker compose 플러그인 (수동)
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# buildx 플러그인 (compose build에 필요)
BUILDX_VER=$(curl -s https://api.github.com/repos/docker/buildx/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
sudo curl -SL "https://github.com/docker/buildx/releases/download/${BUILDX_VER}/buildx-${BUILDX_VER}.linux-amd64" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx

# buildx 권한 이슈 해결
sudo chown -R ec2-user:ec2-user /usr/local/lib/docker

# Tailscale (URL 인증은 로컬 브라우저에서)
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up                            # → 100.120.208.61

# (소형 번들) 스왑 2GB
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

콘솔(웹):
- Lightsail 방화벽: **22(SSH)만** 열고 8080/8090은 인터넷에 열지 않음 (Tailscale로만 접근)
- Tailscale 콘솔: 서버 장치 **key expiry 비활성화**

---

## 3. 로컬(작업 PC/브라우저)에서 한 작업

### 키 & 접속
```bash
ssh-keygen -t ed25519 -f ~/report-deploy-key -N ""   # 개인키→GitHub Secret, 공개키→서버
```
- Mac·iPhone에 Tailscale 설치/로그인(서버와 같은 계정)
- Tailscale OAuth 클라이언트 생성 + ACL에 `tag:ci` 정의

### AWS 콘솔
- S3 버킷 2개: `report-product-bucket`(공개읽기 버킷정책), `report-gift-bucket`(퍼블릭 차단 유지)
- IAM 사용자 + 최소권한(`docker/s3-iam-policy.json`) → 액세스 키 발급

### 코드 변경 (커밋·푸시로 배포)
- `Dockerfile`, `docker/docker-compose.prod.yml`, `docker/.env.prod.example`, `.dockerignore`
- `.github/workflows/deploy.yml` (checkout@v5, Tailscale 합류 → rsync → .env 생성 → compose up → health)
- `application.yml` (DB/Redis host·계정을 `${ENV:기본값}`로), `build.gradle` (`jar { enabled = false }`)
- 앱: `api_config.dart`(`--dart-define=API_BASE_URL`), `Info.plist`(ATS 평문 HTTP 예외)

### 배포·검증·앱 빌드
```bash
git push origin master
curl http://100.120.208.61:8080/actuator/health        # UP 확인
flutter build ipa --dart-define=API_BASE_URL=http://100.120.208.61:8080
```

### 관리자 데이터 (IntelliJ DB + curl)
- IntelliJ Database → SSH 터널(ec2-user@100.120.208.61, 키) → `localhost:5432`
- 관리자 계정: `signup`(USER) → `UPDATE rpt_user SET role='ADMIN' WHERE email=...` → 재로그인

---

## 4. report-admin-api (관리자 API, 선택 배포)

front-api와 **같은 DB/Redis를 공유**(같은 상품·코드·회원 데이터). 별도 DB를 띄우지 않음.

- compose: `name: admin-prod`, 이미지 `report-admin-api:latest`, 호스트 포트 **8090**
- front-api(`daily-prod`)가 만든 네트워크 `daily-prod_default`에 `external`로 합류
- `.env.prod`의 `DB_USER`/`DB_PASSWORD`/`JWT_SECRET`은 front-api와 **동일 값**

```bash
# front-api가 먼저 떠 있어야 함
docker compose -f docker/docker-compose.prod.yml --env-file docker/.env.prod up -d --build
curl http://100.120.208.61:8090/actuator/health
```

> ⚠️ 현재 구성은 두 API가 **같은 호스트**에 있을 때만 동작한다(external 네트워크는 호스트 로컬).
> 서버를 물리적으로 분리하려면 DB/Redis를 공용 위치로 빼야 한다(아래 5장 참고).

---

## 5. 데이터 계층 분리에 대한 메모

admin-api와 front-api는 동일한 도메인 데이터(상품/기프티콘/회원/포인트)를 다루므로 **DB는 공유가 정석**이다.
서버를 분리할지 여부는 "DB를 나눌까"가 아니라 "DB를 어디에 둘까"의 문제다.

- **같은 호스트 + 공유 DB/Redis 컨테이너** (현재): 가장 단순. 현재 규모에 적합.
- **서버 분리 시**: Postgres/Redis를 공용 서비스로 승격(Lightsail managed DB / RDS / ElastiCache 또는 전용 DB 호스트)하고
  두 API가 그 엔드포인트로 접속. 운영 복잡도↑ — 실제 분리 필요가 있을 때만.
