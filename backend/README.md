# Backend API Usage Examples

Base URL: `http://localhost:8080`

Authentication: add header `Authorization: Bearer <JWT>` for protected endpoints.

---
## Auth

### Register
POST `/auth/register`

Request
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Response 201
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER",
  "enabled": false
}
```

### Login
POST `/auth/login`

Request
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Response 200
```json
{
  "token": "<jwt-token>",
  "expiresIn": 3600000
}
```

### Verify email
POST `/auth/verify`

Request
```json
{
  "verificationCode": "123456",
  "email": "user@example.com"
}
```

Response 200
```json
"User verified successfully"
```

### Resend verification
POST `/auth/resend`

Request
```json
{
  "email": "user@example.com"
}
```

Response 200
```json
"Verification email resent successfully"
```

---
## Users

### Get current user
GET `/users/me`

Response 200
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER",
  "enabled": true
}
```

### Get my reservations
GET `/users/me/reservations`

Response 200
```json
[
  {
    "id": 10,
    "email": "user@example.com",
    "roomNumber": 101,
    "roomType": "SINGLE",
    "roomCapacity": 1,
    "roomPricePerNight": 109.99,
    "totalPrice": 219.98,
    "status": "PENDING",
    "checkInDate": "2025-09-10",
    "checkOutDate": "2025-09-12",
    "createdAt": "2025-08-20T12:34:56"
  }
]
```

### Admin: list users
GET `/users`

Response 200
```json
[
  {
    "id": 1,
    "email": "admin@example.com",
    "role": "ADMIN",
    "enabled": true
  },
  {
    "id": 2,
    "email": "user@example.com",
    "role": "USER",
    "enabled": true
  }
]
```

### Admin: get user by id
GET `/users/{userId}`

Response 200
```json
{
  "id": 2,
  "email": "user@example.com",
  "role": "USER",
  "enabled": true
}
```

### Admin: get user reservations
GET `/users/{userId}/reservations`

Response 200 — same shape as "Get my reservations"

---
## Rooms

### Search/list rooms
GET `/rooms`

Query params (all optional): `roomNumber`, `type` (SINGLE|DOUBLE|SUITE|DELUXE), `minCapacity`, `maxPricePerNight`, `checkInDate` (YYYY-MM-DD), `checkOutDate` (YYYY-MM-DD)

Response 200
```json
[
  {
    "id": 5,
    "roomNumber": 101,
    "type": "SINGLE",
    "capacity": 1,
    "pricePerNight": 109.99,
    "description": "Single room with a bathroom and a balcony",
    "bookedDates": [
      {
        "checkInDate": "2025-09-10",
        "checkOutDate": "2025-09-12"
      }
    ]
  }
]
```

### Admin: create room
POST `/rooms`

Request
```json
{
  "roomNumber": 101,
  "roomType": "SINGLE",
  "capacity": 1,
  "pricePerNight": 109.99,
  "description": "Single room with a bathroom and a balcony"
}
```

Response 201
```json
{
  "id": 5,
  "roomNumber": 101,
  "type": "SINGLE",
  "capacity": 1,
  "pricePerNight": 109.99,
  "description": "Single room with a bathroom and a balcony"
}
```

### Admin: update room (partial)
PATCH `/rooms/{roomId}`

Request (any subset)
```json
{
  "roomNumber": 102,
  "roomType": "DOUBLE",
  "capacity": 2,
  "pricePerNight": 149.99,
  "description": "Updated description"
}
```

Response 200 — same shape as create response

### Admin: delete room
DELETE `/rooms/{roomId}`

Response 204 (no content)

---
## Reservations

### Create reservation
POST `/reservations`

Request
```json
{
  "roomId": 5,
  "checkInDate": "2025-09-10",
  "checkOutDate": "2025-09-12"
}
```

Response 201
```json
{
  "id": 10,
  "email": "user@example.com",
  "roomNumber": 101,
  "roomType": "SINGLE",
  "roomCapacity": 1,
  "roomPricePerNight": 109.99,
  "totalPrice": 219.98,
  "status": "PENDING",
  "checkInDate": "2025-09-10",
  "checkOutDate": "2025-09-12",
  "createdAt": "2025-08-20T12:34:56"
}
```

### Confirm reservation
PATCH `/reservations/{reservationId}/confirm`

Response 200 — same shape as create response, with `status` = `CONFIRMED`

### Cancel reservation
PATCH `/reservations/{reservationId}/cancel`

Response 200 — same shape as create response, with `status` = `CANCELLED`

---
## Error responses (example)

Response 400
```json
{
  "message": "Room not available",
  "code": "ROOM_NOT_AVAILABLE"
}
```

Response 403: empty body (forbidden)

Response 404
```json
{
  "message": "Reservation not found"
}
```
