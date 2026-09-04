# EmployeeHub — API Reference

All requests go through the **API Gateway**: `http://localhost:8090`

**Auth model:** log in once via `POST /api/v1/auth/login`; the gateway sets an
`httpOnly` `jwt` cookie. The browser sends it automatically on later requests
(use `credentials: 'include'` / `withCredentials: true` for cross-origin calls).
`Public` = no auth required; everything else needs the `jwt` cookie.

---

## Auth Service  `/api/v1/auth`

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Public | Register a new user |
| POST | `/api/v1/auth/login` | Public | Log in; sets the `jwt` cookie |
| GET | `/api/v1/auth/me` | Cookie | Returns the authenticated user's email |

**Register** — request body → `201 Created`
```json
{ "fullName": "Asha Rao", "email": "asha@example.com", "password": "Password123" }
```
```json
{ "id": 1, "fullName": "Asha Rao", "email": "asha@example.com" }
```

**Login** — request body → `200 OK` (+ `Set-Cookie: jwt=...`)
```json
{ "email": "asha@example.com", "password": "Password123" }
```
```json
{ "id": 1, "fullName": "Asha Rao", "email": "asha@example.com" }
```

---

## Employee Service  `/api/v1/employees`  (all require the `jwt` cookie)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/employees` | List all employee profiles |
| GET | `/api/v1/employees/{id}` | Get one profile by id |
| GET | `/api/v1/employees/me` | Get the caller's own profile (from the JWT) |
| PUT | `/api/v1/employees/{id}` | Complete / update a profile (emits `EmployeeUpdated`) |

**Update** — request body → `200 OK`
```json
{ "department": "Engineering", "designation": "Backend Engineer", "managerId": null, "status": "ACTIVE" }
```
**Employee response shape**
```json
{
  "id": 1,
  "userId": 1,
  "fullName": "Asha Rao",
  "email": "asha@example.com",
  "department": "Engineering",
  "designation": "Backend Engineer",
  "managerId": null,
  "status": "ACTIVE",
  "createdAt": "2026-09-04T10:00:00Z",
  "updatedAt": "2026-09-04T10:05:00Z"
}
```
`status` ∈ `PENDING_ONBOARDING` · `ACTIVE` · `INACTIVE`

---

## Audit Service  `/api/v1/audit`  (requires the `jwt` cookie)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/audit` | List recorded events (newest first) |

**Audit response shape**
```json
[
  {
    "id": 1,
    "subject": "asha@example.com",
    "eventType": "UserRegistered",
    "sourceTopic": "employeehub.USER",
    "details": "User Asha Rao (id 1) registered.",
    "recordedAt": "2026-09-04T10:00:01Z"
  }
]
```

---

## Health checks (direct service ports, not via gateway)

| Service | URL |
| --- | --- |
| api-gateway | `http://localhost:8090/actuator/health` |
| auth-service | `http://localhost:8080/api/v1/health` |
| employee-service | `http://localhost:8082/api/v1/health` |
| audit-service | `http://localhost:8081/actuator/health` |

