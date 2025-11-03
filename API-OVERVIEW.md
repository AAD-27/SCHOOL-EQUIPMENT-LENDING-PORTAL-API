# API Overview

Base URL: http://localhost:8083/equipment-service

Use the Authorization header with Bearer tokens on all protected endpoints: Authorization: Bearer TOKEN-...

## Auth 🔐
- POST /api/auth/signup — Signup for student, staff, or admin. Public (no token). Returns token + user info.
  - Validation behavior: returns status code 210 (custom) with message "Email id already exists" when the email is already registered.
- POST /api/auth/login — Login and receive a new token + user info. Public (no token).
  - Validation behavior: returns status code 210 (custom) with message "Invalid password" when email exists but password is wrong; returns 401 "Invalid credentials" when email is not found or no match.
- POST /api/auth/logout — Logout and invalidate the token (requires token).
- GET /api/auth/roles — List all roles. Public (no token).
- GET /api/auth/categories — List all categories. Public (no token).

Behavior
- Token is generated on successful signup/login and must be sent on protected requests.
- Roles are part of the returned user info and drive access control across the API.

## Equipment 📦
- GET /api/equipment — List all equipment (optional: category query param) (requires token).
- GET /api/equipment/{id} — Get item details (requires token).
- POST /api/equipment — Create an item (ADMIN only, requires token).
- PUT /api/equipment/{id} — Update an item (ADMIN only, requires token).
- DELETE /api/equipment/{id} — Delete an item (ADMIN only, requires token).

Data fields per item
- name, category, conditionDescription, quantity, availableQuantity

Status codes
- 200 OK with the saved entity for create/update.
- 200 OK with a success message for delete.
- 401 if no/invalid token.
- 403 if token is not ADMIN.
- 404 when updating/deleting a non-existent item.

Functional behavior
- Admin can add/edit/delete. UI shows management controls when logged in as admin.
- Student login hides equipment management in UI; calling those endpoints returns 403 🚫.

## Borrow 🔁
- POST /api/borrow/request — Create a borrow request (student) (requires token). Validates availability.
  - Validation behavior: returns status code 210 (custom) with a message instead of 400 when validation fails, for example:
    - "Please enter a valid quantity"
    - "Projector is invalid"
    - "Projector is not available"
- GET /api/borrow — List borrow requests (requires token). Students see their own; staff/admin see all.
- POST /api/borrow/{id}/approve — Approve (STAFF or ADMIN, requires token). Decreases availableQuantity. ✅
  - Validation behavior: returns status code 210 (custom) with message "Insufficient quantity to approve" when stock is not enough.
- POST /api/borrow/{id}/reject — Reject (STAFF or ADMIN, requires token). Doesn't change inventory. ❌
- POST /api/borrow/{id}/return — Mark returned (STAFF or ADMIN, requires token). Increases availableQuantity.

Rules
- Prevents approving when availableQuantity is insufficient.
- Request lifecycle: PENDING → APPROVED → RETURNED. REJECTED is also supported via the reject endpoint.

Notes
- Tokens are simulated (in-memory map). New token each login/signup.
- Passwords and token handling are simplified for demonstration.

## Functional behavior (summary) ✨

1. User Authentication & Roles 🔐
- Login/signup for students, staff, and admins
- Role-based access (student, staff, admin)
- Simple token-based login (simulated)

2. Equipment Management 📦
- Add, edit, or delete items (by admin)
- Each item has a name, category, condition, quantity, and availability

3. Borrowing & Return Requests 🔁
- Students can request equipment
- Staff/admin approves or rejects requests
- Mark as returned when completed
- Prevent overlapping bookings for the same item
