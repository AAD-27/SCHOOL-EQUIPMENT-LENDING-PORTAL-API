# School Equipment Lending Portal – API

A small REST API that helps a school lend equipment to students and staff. It supports user login, role-based permissions, managing inventory, and handling borrow/return requests.

## How authentication works 🔐
1. A user tries to log in (or sign up) with email and password.
2. The app validates the credentials against the database.
3. On success, the app generates a unique access token for that session.
4. The client sends this token with future requests (Authorization header).
5. The server checks the token to identify the user and enforce role-based access.

## What you can do 📦
1. User Authentication & Roles
   - Login/signup for students, staff, and admins
   - Role-based access (student, staff, admin)
   - Simple token-based login (simulated)

2. Equipment Management
   - Add, edit, or delete items (by admin)
   - Each item has a name, category, condition, quantity, and availability

3. Borrowing & Return Requests 🔁
   - Students can request equipment
   - Staff/admin approves or rejects requests
   - Mark as returned when completed
   - Prevent overlapping bookings for the same item

## Equipment management behavior and status codes
- When an admin logs in:
  - They can add, edit, and delete equipment via the UI and APIs.
  - API responses:
    - 200 OK with the saved entity for create/update.
    - 200 OK with a success message for delete.
    - 401 if no/invalid token.
    - 403 if token is not ADMIN.
    - 404 when updating/deleting a non-existent item.

- When a student logs in:
  - The UI hides add/edit/delete actions.
  - If the student still calls those APIs, the response is 403 Forbidden 🚫.
