# Equipment Lending Portal — Complete API & System Documentation

## 1. Overview
This documentation covers the REST APIs for a School Equipment Lending Portal: authentication, equipment management, and borrow/return workflows. It documents endpoints (requests/responses), RBAC rules, DB schema, system architecture, component hierarchy for the React frontend, example usage, and assumptions.

---

## 2. Summary of Endpoints

### Auth Service (Public + Protected)
| Method | Endpoint | Description | Auth Required |
|-------|----------|-------------|---------------|
| GET | /api/auth/roles | List available user roles | No |
| GET | /api/auth/categories | List equipment categories | No |
| POST | /api/auth/login | Login user & return token | No |
| POST | /api/auth/signup | Create user & return token | No |

### Equipment Service (Admin Only for Modifications)
| Method | Endpoint | Description | Auth (Role) |
|-------|---------|-------------|-------------|
| GET | /api/equipment | List all equipment | Yes |
| POST | /api/equipment | Add equipment | Admin |
| PUT | /api/equipment/{id} | Edit equipment | Admin |
| DELETE | /api/equipment/{id} | Delete equipment | Admin |

### Borrow Service (Role Based Behavior)
| Method | Endpoint | Description | Allowed Roles |
|--------|----------|-------------|---------------|
| POST | /api/borrow/request | Submit borrow request | Student |
| GET | /api/borrow | View borrow requests | Student (Self) / Staff/Admin (All) |
| POST | /api/borrow/{id}/approve | Approve request | Staff/Admin |
| POST | /api/borrow/{id}/reject | Reject request | Staff/Admin |
| POST | /api/borrow/{id}/return | Mark returned | Staff/Admin |

---

## 3. Auth & Token Usage
- **Token-Based Authentication** (Bearer token)
- Pass token in header: `Authorization: Bearer <TOKEN>`
- Roles: `STUDENT`, `STAFF`, `ADMIN`

---

## 4. API Reference (Detailed)

### 4.1 Auth Service

#### `POST /api/auth/login`
**Sample Request**
```json
{ "email": "admin@school.com", "password": "admin123" }
```
**Sample Response**
```json
{
  "token": "TOKEN-XXXX",
  "userId": 1,
  "name": "Admin User",
  "email": "admin@school.com",
  "role": "ADMIN"
}
```

#### `POST /api/auth/signup`
```json
{
  "name": "Ameya",
  "email": "amey@bits.com",
  "password": "amey123",
  "role": "STUDENT"
}
```

---

### 4.2 Equipment Service

#### `GET /api/equipment`
```json
[
  {
    "id": 1,
    "name": "Laptop Dell",
    "category": "Electronics",
    "quantity": 10,
    "availableQuantity": 10
  }
]
```

#### `POST /api/equipment`
```json
{
  "name": "Keyboard",
  "category": "Electronics",
  "conditionDescription": "Good",
  "quantity": 13,
  "availableQuantity": 5
}
```

---

### 4.3 Borrow Service

#### `POST /api/borrow/request`
```json
{
  "equipment": { "id": 1 },
  "quantityRequested": 2,
  "fromDate": "2025-11-05T09:00:00",
  "toDate": "2025-11-06T17:00:00"
}
```

#### Response (Success)
```json
{ "status": "PENDING", "equipment": { "id": 1, "name": "Laptop Dell" } }
```

#### Response (Validation Failure)
```
"Insufficient quantity"
```

---

## 5. Database Schema (ER Diagram)

```
Users (id, name, email, password_hash, role)
Categories (id, name)
Equipment (id, name, category_id*, condition_description, quantity, available_quantity)
BorrowRequests (id, requester_id*, equipment_id*, quantity_requested, from_date, to_date, status)
```

Relationships:
- Users (1) —— (M) BorrowRequests
- Equipment (1) —— (M) BorrowRequests
- Categories (1) —— (M) Equipment

---

## 6. System Architecture

**Client (React UI)** → **Node/Java Spring Boot Backend** → **Database (MySQL/PostgreSQL)**

```
[React UI] → [REST API] → [Service Layer] → [Database]
```

---

## 7. Component Hierarchy (React)

```
App
 ├── LoginPage
 ├── SignupPage
 ├── Dashboard
 │     ├── EquipmentList
 │     ├── BorrowRequestList
 │     └── AdminEquipmentManager
 └── RequestBorrowForm
```

---

## 8. Example User Flow

1. Student logs in → receives token.
2. Student views equipment list.
3. Student submits borrow request.
4. Staff/Admin reviews request, approves or rejects.
5. If approved, item is returned later and status updated.

---

## 9. Assumptions

- Only Admin can modify equipment.
- Only Staff/Admin may approve/reject/return.
- Student sees only their own requests.
- Time conflicts are not deeply validated at the API level (optional enhancement).

---

## End of Document
