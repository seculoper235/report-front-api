# 포인트(totalPoint)로 기프티콘 교환 기능 구축 계획

> 최초 작성: 2026-06-19 · 개정: 2026-06-19 (서버/클라 API 연동 완료) ·
> **재개정: 2026-06-19 (서버 인증 완료 반영, 발급 방식=코드 풀 확정)**
> 나중에 이 문서를 보고 남은 작업을 구현하기 위한 설계 메모.

## 1. 배경 (왜 이 작업을 하는가)

이 기능의 목표는 사용자가 쌓은 **포인트(totalPoint)** 로 **기프티콘**(모바일 쿠폰)을 교환하는 것이다.
기프티콘은 금전적 가치를 가지므로 **"포인트 잔액·차감의 정합성"** 이 가장 중요한 요구사항이다.

> ⚠️ **이 문서의 1차 버전은 "앱이 순수 Flutter 클라이언트이고 포인트가 메모리 더미로 계산된다"는 전제**였으나,
> 그 사이에 **백엔드 서버와 클라이언트 API 연동이 대부분 완료**되었다. 아래 2장에 현재 실제 상태를 정리하고,
> 3장부터 기프티콘 교환을 위해 **남은 작업**을 설계한다.

확정된 전제:
- 백엔드: **Spring Boot 4.1 + 관계형 DB** (개발/테스트 H2, 운영 PostgreSQL) — 이미 구동 중인 `report-front-api` 레포
- 교환 대상: **기프티콘** (실물 배송이 아닌 모바일 쿠폰)
- 범위: **백엔드 + 클라이언트 전체**

---

## 2. 현재 상태 (이미 구축된 것) ✅

### 2-1. 백엔드 `report-front-api` (별도 Gradle 레포, 이미 존재)

- 스택: **Spring Boot 4.1.0, Java 21, Jackson 3(`tools.jackson`), QueryDSL(openfeign 7.0), Spring Data JPA,
  Liquibase, Bean Validation**. DB는 H2(dev) + PostgreSQL(운영). **Spring Security/JWT는 아직 없음.**
- 공통: `ApiResponse<T>`(`{code, message, body}`) 응답 래퍼, `BaseEntity`(생성/수정 감사),
  `CodeEnum` + `CodeEnumModule`(Jackson) — **CodeEnum은 name이 아니라 code로 (역)직렬화**.
- 도메인:
  - **cost** (`/api/costs`): `ReportCost`(categoryName, costName, fixedYn, costDescription,
    amountDivision=CostAmountDivision[INCREASE/DECREASE], costAmount[BigInteger], paymentMethod=PaymentMethod,
    paymentAt, costDivision[GOOD/BAD], costPoint). 무한스크롤/리포트 쿼리는 QueryDSL `ReportCostRepository`.
  - **habit** (`/api/habits`): `ReportHabit`(habitName, habitDivision[GOOD/BAD], habitPoint) + 일/월 집계.
  - **auth/user**: **인증 구현 완료** ✅ — Spring Security + JWT(jjwt) + Refresh/Blacklist, `User`(+`Role`),
    `AuthController`(`/api/auth/signup·login·reissue·logout`), `SecurityUtil.getCurrentUserId()`.
    `/api/auth/**`·`/h2-console/**`만 공개, **나머지는 모두 인증 필요**(`anyRequest().authenticated()`).
    `auditorProvider`도 로그인 사용자 ID로 연결됨(`crt_by`에 사용자 기록).
  - ⚠️ 단, **cost/habit는 아직 사용자 스코프가 아님** — 조회가 `crt_by`(소유자) 기준으로 필터되지 않아 전 사용자 공유 상태.
- CodeEnum code 값(참고): PaymentMethod `RP010001~RP010004`(현금/신용카드/체크카드/계좌이체),
  CostAmountDivision `RP020001`(INCREASE/입금)·`RP020002`(DECREASE/출금).

현재 cost 엔드포인트:
- `POST /api/costs` (등록), `PUT /api/costs/{id}`, `DELETE /api/costs/{id}`, `GET /api/costs/{id}`
- `GET /api/costs?category=` (카테고리별 목록)
- `GET /api/costs/search?page=&size=&sort=&division=&startDate=&endDate=` (무한스크롤 페이징)
- **`GET /api/costs/point` → 전체 순포인트 합계**(GOOD +costPoint, BAD −costPoint). 메인 헤더의 totalPoint
- `GET /api/costs/calendar|week|month?year=&month=` (수입/지출 금액 집계)

### 2-2. 클라이언트 `report_application` (Flutter)

- 네트워킹: **`http` 패키지** 사용(※ 1차 계획의 `dio`가 아님). `lib/api/api_config.dart`(baseUrl),
  `lib/api/cost_api.dart`, `lib/api/habit_api.dart`.
- 스토어: `lib/store/cost_store.dart`·`habit_store.dart` → **더미/로컬 합산 제거, "변경 알림"만 하는 ChangeNotifier**.
  데이터의 원천은 서버.
- 화면(모두 API 연동 완료):
  - `cost_view.dart`: `GET /api/costs/search` 무한스크롤 + 헤더 totalPoint는 `GET /api/costs/point`.
  - `create_account.dart`: 등록/수정/삭제(`POST/PUT/DELETE /api/costs`). 입금/출금(amountDivision)·소비유형(costDivision,
    **선택 사항**)·결제수단은 **code 값**으로 전송.
  - `account_detail.dart`: `GET /api/costs?category=`.
  - `total_account.dart`: `calendar/week/month` 금액 리포트.
  - `habit_view.dart`/`create_habit.dart`: `/api/habits` 일/월/등록.
  - `daliy_view.dart`(메인 경험치 링): **아직 하드코딩, 미연동**(목표 XP 개념이 서버에 없어 보류).

### 2-3. 기프티콘 관점에서의 "현재 한계" (= 남은 작업의 출발점)

1. **포인트가 "잔액"이 아니라 "점수 합"이다.** `totalPoint = Σ(cost net point)`일 뿐, **차감(교환) 개념이 없다.**
   교환으로 포인트를 쓰려면 차감을 반영한 잔액이 필요하다.
2. **인증/사용자가 없다.** 단일 사용자 가정이라 "누구의 잔액인지"가 없다. 금전적 가치를 다루므로 사용자 식별이 필수.
3. **상품(기프티콘 카탈로그)·교환 주문·벤더 발급이 전부 없다.**

---

## 3. 목표 아키텍처 (남은 작업 반영)

```
[Flutter App]
  - (신규) 로그인(JWT 획득) → flutter_secure_storage 보관
  - 포인트 잔액/원장 조회            GET /api/points/balance, /api/points/ledger
  - 소비 등록(적립)                  POST /api/costs            (이미 구현)
  - (신규) 기프티콘 상점 목록        GET /api/products
  - (신규) 교환 요청(멱등키 동봉)    POST /api/redemptions  → 발급된 기프티콘
  - (신규) 교환 내역                 GET /api/redemptions
        |
        v  (HTTPS, Bearer JWT)
[Spring Boot API  · report-front-api 에 도메인 추가]
  - (신규) 인증/인가 (Spring Security + JWT) + user 도메인 채우기
  - (신규) 포인트 잔액 = cost 순포인트 − 교환 차감  (원장 도입 권장)
  - (신규) @Transactional 교환 처리(잔액 잠금 → 차감 → 주문 → 재고에서 코드 지급)
  - (신규) 기프티콘 발급 = 코드 재고(풀)에서 미사용 코드 pop  ← 외부 벤더 계약 없이 시작
        |
        +--> [DB]  user / point_ledger / product / gift_inventory / redemption_order  (+ 기존 rpt_cost, rpt_habit)
        +--> (선택·후순위) [기프티콘 벤더 API]  GifticonVendor 구현체 교체로 자동 발급 전환
```

> 발급 방식: **코드 풀(재고) 방식**으로 시작한다. 운영자가 미리(또는 주문이 들어올 때) 구매한 기프티콘
> 코드/핀번호를 `gift_inventory`에 넣어두고, 교환 시 **미사용 코드 1개를 꺼내(pop) 지급**한다.
> 외부 발급사 계약·예치금 없이 **실제 기프티콘 지급**이 가능하며, 나중에 자동화가 필요하면 같은
> `GifticonVendor` 추상화에서 B2B API 구현체로 교체한다.

---

## 4. Part A — 백엔드 (report-front-api 에 **도메인 추가**)

> 신규 레포가 아니다. 기존 레포의 컨벤션을 재사용한다: `domain/<name>/{controller,application,repository}`,
> `ApiResponse`, `BaseEntity`, QueryDSL `BaseRepository`, Liquibase 마이그레이션, `CodeEnum`(code 직렬화).

### A-1. 인증 / 사용자  ✅ 완료

- Spring Security + JWT(jjwt 0.12) + Refresh/Blacklist, `User`/`Role`, `/api/auth/signup·login·reissue·logout`.
- `anyRequest().authenticated()` (공개: `/api/auth/**`, `/h2-console/**`). `auditorProvider` → 로그인 사용자 ID.
- 남은 일부: **cost/habit 사용자 스코프화**(A-1b) — 데이터 소유권 분리. (포인트가 사용자별이어야 하므로 선행)

### A-1b. cost/habit 사용자 스코프화 (진행 대상)

- 별도 `user_id` 컬럼을 추가하지 않고 **기존 `crt_by`(createdBy)를 소유자로 재사용**(마이그레이션 불필요,
  생성 시 감사로 자동 기록됨).
- 모든 조회/검색/집계/단건/수정/삭제를 **`createdBy == 현재 사용자`** 로 제한
  (`SecurityUtil.getCurrentUserId()`). 타 사용자 행은 not-found 처리.

### A-2. 포인트 잔액 모델 (이 기능의 핵심 정합성)

현재는 잔액 = `ReportCostRepository.sumNetPoint()`(전체 cost 순합)뿐이라 **차감을 표현할 수 없다.** 두 가지 선택:

- **(권장) 포인트 원장 `point_ledger` 도입** — 단일 진실원:
  - id, user_id, delta(+적립/−차감), reason(enum: EARN_COST, REDEEM, ADJUST, REFUND), ref_type, ref_id, created_at
  - **잔액 = `SELECT COALESCE(SUM(delta),0) FROM point_ledger WHERE user_id=?`**
  - cost 등록 시 EARN_COST delta 기록, 교환 시 REDEEM(−) 기록. (기존 cost point 합산 로직과 정합성 맞추기)
  - 성능 필요 시 `user.point_balance` 캐시 컬럼(항상 같은 트랜잭션에서 갱신).
- **(최소안) 차감만 별도 집계** — 원장 없이 `잔액 = sumNetPoint(cost) − Σ(redemption.point_cost[SUCCESS])`.
  빠르지만 적립/조정 이력이 안 남아 확장성이 떨어진다. MVP 한정.

### A-3. 도메인: 상품 / 코드 재고 / 교환

- `product` (기프티콘 카탈로그): id, name, brand, image_url, point_cost, active
  - 재고 수량은 별도 컬럼이 아니라 **`gift_inventory`의 미사용(AVAILABLE) 코드 개수**로 파생.
- `gift_inventory` (**코드 풀** — 이 방식의 핵심): id, product_id,
  code(또는 pin_no), barcode_image_url(선택), valid_until(선택),
  status(enum: AVAILABLE/RESERVED/ISSUED), redemption_order_id(지급된 주문, nullable), created_at
  - **코드는 민감정보** → 컬럼 암호화 또는 최소한 접근 통제. 평문 로그 금지.
- `redemption_order`: id, user_id, product_id, point_cost(주문 시점 스냅샷),
  status(enum: ISSUED/WAITING_STOCK/CANCELED), idempotency_key(**unique**),
  gift_inventory_id(지급된 코드, nullable), created_at, updated_at
  - 정책 선택: 재고가 없을 때 **(a) 즉시 실패(차감 안 함)** 또는 **(b) WAITING_STOCK으로 차감 후 대기**
    (운영자가 코드 투입 시 자동 ISSUED 완료). MVP는 (a) 즉시 실패가 단순하고 안전.

### A-4. 교환 트랜잭션 로직 (심장) — `RedemptionService.redeem(userId, productId, idempotencyKey)` `@Transactional`

1. **멱등 체크**: idempotency_key로 기존 주문 조회 → 있으면 그대로 반환(중복 차감 방지).
2. 상품 조회 + active 확인.
3. **잔액 잠금 후 검증**: 사용자 행 비관적 락(`SELECT ... FOR UPDATE`)으로 동시성 차단 → 잔액 계산 →
   `balance >= product.point_cost` 아니면 `InsufficientPointException`.
4. **코드 재고 확보**: `gift_inventory`에서 해당 product의 `AVAILABLE` 코드 1건을 **행 잠금으로 pop**
   (`SELECT ... FOR UPDATE SKIP LOCKED` 권장 — 동시 교환 시 같은 코드 중복 지급 방지) → 상태 RESERVED/ISSUED.
   - 재고 0이면 정책에 따라 즉시 실패(차감 안 함) 또는 WAITING_STOCK.
5. `point_ledger`에 `delta = -point_cost`, reason=REDEEM 기록(+캐시 갱신).
6. `redemption_order` ISSUED 생성 + 지급한 `gift_inventory_id` 연결, 코드 status=ISSUED.
7. 발급 결과(쿠폰코드/바코드 이미지/유효기간) 반환.

> 외부 I/O가 없어 **한 트랜잭션 안에서 차감+코드 지급이 원자적으로** 끝난다(벤더 API 방식보다 단순/안전).
> 취소가 필요하면 코드 status를 AVAILABLE로 되돌리고 REFUND ledger 기록.

### A-5. 코드 재고(풀) 운영 — 발급 방식

- 발급 = **외부 호출 없이 `gift_inventory`에서 미사용 코드 pop**. 발급사 계약·예치금 불필요.
- **재고 적재(운영자 전용)**: 운영자가 구매한 코드/핀번호(+바코드 이미지·유효기간)를 관리자 API로 등록.
  미리 소량 버퍼를 넣어두면 즉시 지급, 0으로 두면 주문 들어올 때 채우는 **주문형(JIT)** 운영도 가능
  (둘은 버퍼 크기만 다른 같은 모델).
- **전달 형태 주의**: 코드/핀번호·바코드로 떨어지는 상품만 앱에 그대로 표시 가능. "카톡 선물하기처럼
  상대에게 직접 전송"되는 상품은 코드가 없어 이 방식에 부적합(운영자 수동 전달 대상).
- **추상화 유지**: `GifticonVendor` 인터페이스의 기본 구현 = `InventoryGifticonVendor`(DB 코드 pop).
  훗날 자동화가 필요하면 동일 인터페이스의 B2B API 구현체로 교체(이 문서 범위 밖, 후순위).

### A-6. 신규 REST API (초안) — 기존 `ApiResponse` 래퍼/`/api` 프리픽스 준수

- `POST /api/auth/signup`, `POST /api/auth/login` → JWT
- `GET  /api/points/balance` → `{ balance }`  (기존 `/api/costs/point`을 잔액 개념으로 흡수/대체 검토)
- `GET  /api/points/ledger?cursor=` → 적립/차감 내역(페이지네이션, 기존 `PageResponse` 재사용)
- `GET  /api/products` → 카탈로그(상품별 재고 보유 여부 포함)
- `POST /api/redemptions` body: `{ productId, idempotencyKey }` → 교환(차감 + 코드 지급)
- `GET  /api/redemptions?cursor=` → 교환 내역(지급된 코드/바코드/유효기간 포함)
- **(운영자 전용)** `POST /api/admin/products`, `POST /api/admin/products/{id}/codes`
  (코드 재고 적재) — 관리자 권한으로만 접근. 별도 관리 도구가 없으면 우선 이 엔드포인트로 운영.

---

## 5. Part B — Flutter 클라이언트 (남은 작업)

> 네트워킹 계층(`http`, `lib/api/*`)과 store(알림 전용)는 **이미 있다.** 그 패턴을 그대로 확장한다.

### B-1. 인증 토대 (신규)

- `pubspec.yaml`에 `flutter_secure_storage`(+필요 시 `uuid`) 추가.
- `lib/api/api_config.dart`/공통 클라이언트에 **Authorization 헤더 주입 + 401 처리**.
  (현재 각 API가 `http.Client`를 직접 쓰므로, 토큰 헤더를 공통 주입하는 작은 래퍼 도입 권장.)
- `lib/api/auth_api.dart`(signup/login), `lib/pages/auth/login_page.dart`, 앱 시작 시 토큰 유무로 분기(`main.dart`).

### B-2. 포인트 잔액 (기존 연동 보정)

- 헤더 totalPoint를 **잔액 엔드포인트**(`GET /api/points/balance`)로 전환(현재는 `GET /api/costs/point`).
  교환 차감이 반영되어야 하므로 A-2의 잔액 정의를 따른다.
- `lib/api/point_api.dart`(balance/ledger) 추가, `CostStore.notifyChanged()` 시 잔액도 새로고침.

### B-3. 기프티콘 상점·교환 UI (신규)

- `lib/api/product_api.dart`, `lib/api/redemption_api.dart` + DTO(`Product`, `RedemptionOrder`).
- `lib/pages/shop/product_list_view.dart`: `GET /api/products` 그리드. 상품별 point_cost와 현재 잔액 비교해 교환 가능 여부 표시.
- `lib/pages/shop/redemption_confirm.dart`: 확인 시트 → `POST /api/redemptions`
  (**클라에서 `idempotencyKey`(uuid) 생성**해 중복 탭/재시도 안전) → 지급된 코드/바코드 이미지 표시.
  재고 0이면 "재고 준비중" 안내(정책 (a)면 교환 불가 처리).
- `lib/pages/shop/redemption_history.dart`: `GET /api/redemptions` (지난 교환의 코드·바코드 다시 보기).
- 진입점: `main.dart`의 4번째 placeholder 탭(`Icon(Icons.directions_bike)`)을 **"상점" 탭**으로 교체하거나,
  `cost_view.dart`의 `_PointHeader`에 "교환하기" 버튼 추가.

---

## 6. 권장 구현 순서 (남은 것만)

> "서버 스캐폴딩 / cost·habit API / 클라 API 전환 / **서버 인증(A-1)**"은 **이미 완료**됨.

1. **(진행 중) cost/habit 사용자 스코프화**(A-1b): `createdBy` 기준 조회 제한.
2. **클라 인증 연동**(B-1): 로그인 화면 + 토큰 저장/헤더 주입(서버 인증은 이미 됨).
3. **포인트 잔액 재정의**: `point_ledger`(권장) 도입 + `GET /api/points/balance|ledger`. cost 적립을 ledger로 일원화.
   클라 헤더를 잔액 엔드포인트로 전환.
4. **상점/교환(코드 풀)**: `product` + `gift_inventory` + 운영자 코드 적재 API → `POST /api/redemptions`
   (트랜잭션 차감 + 재고 코드 pop) + 클라 상점/교환/내역 UI. **여기까지면 실제 기프티콘 지급이 동작**한다.
5. (후순위·선택) **자동 발급 전환**: 물량/운영 부담이 커지면 `GifticonVendor`를 B2B API 구현체로 교체.

## 7. 검증 (테스트 방법)

- **백엔드 통합 테스트**(핵심): 동시 교환에도 잔액 음수 불가(동시성/락), **같은 코드가 두 명에게 중복 지급되지 않음**
  (재고 pop 시 `FOR UPDATE SKIP LOCKED`), 멱등키 재요청 시 1회만 차감, 재고 0일 때 정책대로 처리.
  기존 `spring-boot-starter-data-jpa-test` + (가능하면) Testcontainers(Postgres), 최소한 H2로라도 검증.
- **API 수동 검증**: signup→login→코드 재고 적재(`POST /api/admin/products/{id}/codes`)→`POST /api/costs` 적립→
  `GET /api/points/balance` 확인→`POST /api/redemptions` 교환→잔액 차감·코드 지급 확인→`GET /api/redemptions` 반영 (curl/Postman).
- **클라 E2E**: `flutter run` → 로그인 → 소비 등록 시 잔액 증가 → 상점 교환 → 헤더 잔액 차감 → 교환 내역 표시.
  잔액 부족 상품은 교환 버튼 비활성/에러 노출.

## 8. 주의/정책 메모

- **기프티콘 교환(포인트→상품)에는 Apple/Google IAP가 불필요**(실물성 상품). 단, 추후 "포인트를 현금 충전"하면
  전자금융거래법(선불전자지급수단) 검토 필요.
- 잔액은 반드시 **서버가 단일 진실원**. 클라 값은 표시용 캐시.
- 교환 요청은 항상 **멱등키**로 보호(중복 차감/이중 발급 방지).
- 서버 **인증은 완료**됨. 다만 cost/habit가 사용자 스코프가 되어야 "사용자별 포인트 잔액"이 성립하므로 A-1b가 선행.
- **코드 풀 운영 메모**: 코드/핀번호는 민감정보 → DB 암호화·접근통제·로그 마스킹. 재고 코드 pop은 반드시
  **행 잠금(`FOR UPDATE SKIP LOCKED`)**으로 중복 지급 방지. 발급 후에는 환불·재판매가 어려우므로
  교환 전 **확인 단계**와 **재고 0 처리 정책**을 명확히 한다. 코드가 아닌 "선물하기 전송형" 상품은 이 방식 대상이 아님.
