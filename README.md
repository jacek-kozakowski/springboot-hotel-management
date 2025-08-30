# 🏨 Hotel Reservation System

A comprehensive **Spring Boot-based backend** for a hotel reservation system with **JWT authentication**, **email verification**, and **role-based access control**. This project is primarily intended to **showcase backend development skills** – including secure authentication, RESTful API design, and integration with a relational database.

To demonstrate and test the backend functionality, a simple **React-based frontend** is included for **presentation purposes only**. The frontend is **not production-ready** and **does not reflect my frontend development abilities**.

If you want to test the backend functionality without frontend I strongly suggest using **Postman**. Check [Backend README](./backend/README.md) for usage examples. 

---

## ✨ Features

- **🔐 JWT Authentication** - Secure login/register with token-based auth
- **📧 Email Verification** - User account verification via email codes
- **🏠 Room Management** - CRUD operations for hotel rooms with availability checking
- **📅 Reservation System** - Book rooms with date validation and conflict prevention
- **👥 Role-Based Access** - Admin/user roles with appropriate permissions
- **📊 RESTful API** - Clean JSON API with comprehensive error handling
- **📝 Logging with SLF4J** - Structured logging for debugging and monitoring

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 3.5.5**
- **Spring Security** + **JWT** authentication
- **PostgreSQL** database
- **Lombok** for cleaner code
- **Maven** build tool
- **JUnit 5** + **Mockito** for testing
- **SLF4J** for logging
- **Docker** + **Docker Compose** for contanainerization
---

## 🚀 Quick Start

### Option 1: Docker (Recommended)

**Prerequisites:**
- Docker
- Docker Compose

**Quick Start with Docker:**

1. **Clone the repository**
```bash
git clone https://github.com/jacek-kozakowski/springboot-hotel-management.git
cd springboot-hotel-management
```

2. **Configure environment variables**

If you are using gmail you can create your app password from [App passwords](https://myaccount.google.com/apppasswords)

**Option A: Using .env file (Recommended)**
Copy the example file and update it with your values:
```bash
cp env.example .env
```

Edit `.env` file with your actual configuration:
```dotenv
JWT_SECRET=your-super-secret-jwt-key-change-in-production
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**Option B: Edit docker-compose.yml directly**
Edit `docker-compose.yml` and update these values:
```yaml
environment:
  JWT_SECRET: your-super-secret-jwt-key-change-in-production
  MAIL_USERNAME: your-email@gmail.com
  MAIL_PASSWORD: your-app-password
```

3. **Run the application**
```bash
docker-compose up --build
```

4. **Access the application**
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Database**: localhost:5433


**Useful Docker commands:**
```bash
# Run in background
docker-compose up -d

# View logs
docker-compose logs

# Stop all services
docker-compose down

# Stop and remove data
docker-compose down -v
```

### Option 2: Manual Installation

**Prerequisites:**
- Java 21
- Maven 3.6+
- PostgreSQL 14+
- Node.js 20+ (for frontend)

**Backend Installation:**

1. **Clone the repository**
```bash
git clone https://github.com/jacek-kozakowski/springboot-hotel-management.git
cd springboot-hotel-management/backend
```

2. **Set Java version**

**MacOS:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version
```

**Windows:**
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
java -version
```

3. **Configure PostgreSQL database**
You can change the values if necessary.

First you need to log in as your superuser (change postgres to your username).
```bash
psql -U postgres
```
```sql
CREATE DATABASE hotel_db;
CREATE USER hotel_user WITH PASSWORD 'password123';
ALTER DATABASE hotel_db OWNER TO hotel_user;
\q
```

4. **Set up `application.properties` or `.env` file**

`application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_db
spring.datasource.username=hotel_user
spring.datasource.password=password123
jwt.secret=YourSecretKeyHere
spring.mail.username=your.email@gmail.com
spring.mail.password=your-app-password
```

or use `.env`:
```dotenv
DATABASE_URL=jdbc:postgresql://localhost:5432/hotel_db
DATABASE_USERNAME=hotel_user
DATABASE_PASSWORD=password123
JWT_SECRET=YourSecretJwtKey
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=your-app-password
```

5. **Run the application**

**MacOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```powershell
.\mvnw spring-boot:run
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/reservations/hotel/
│   │   ├── config/          # Security & application config
│   │   ├── controllers/     # REST endpoints
│   │   ├── dto/             # Data transfer objects
│   │   ├── exceptions/      # Custom exceptions
│   │   ├── models/          # JPA entities
│   │   ├── repositories/    # Spring Data repositories
│   │   ├── services/        # Business logic
│   │   └── HotelApplication.java
│   └── resources/
│       └── application.properties
└── test/                   # Unit & integration tests
```

---

## 🔌 API Endpoints

For usage examples check [Backend README](./backend/README.md)

### User
- `GET /users/me` - Retrieves current user's data
- `GET /users/me/reservations` - Retrieves current user's reservations
- `GET /users` - Retrieves all users (Admin only)
- `GET /users/{userId}` - Retrieves specific user's data (Admin only)
- `GET /users/{userId}/reservations` - Retrieves specific user's reservations (Admin only)

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login with JWT response
- `POST /auth/verify` - Verify email address
- `POST /auth/resend` - Resend verification code

### Rooms
- `GET /rooms` - Search/filter rooms (if no params, returns all)
  - Params: `roomNumber`, `type`, `minCapacity`, `maxPricePerNight`, `checkInDate`, `checkOutDate`
- `POST /rooms` - Add new room (Admin only)
- `PATCH /rooms/{roomId}` - Update room details (Admin only)
- `DELETE /rooms/{roomId}` - Delete room (Admin only)

### Reservations
- `POST /reservations` - Create new reservation
- `PATCH /reservations/{reservationId}/confirm` - Confirm reservation
- `PATCH /reservations/{reservationId}/cancel` - Cancel reservation

---

## Frontend (For Presentation Purposes Only)

A modern **React frontend** is included to demonstrate the backend functionality. **It is not production-ready**. It was made with help of Cursor and it is solely for **presentation purposes** and does not reflect my full frontend development capabilities.

### Features
- User registration and login (JWT-based)
- View rooms and make reservations
- Admin panel for managing rooms and users

### Technology Stack
- React 19
- Material-UI 7
- React Router 7
- Axios for API communication
- Date-fns for date handling
- Vite as build tool

### Installation

**Option 1: With Docker (Recommended)**
The frontend is automatically included when using Docker. See the Docker section above.

**Option 2: Manual Installation**

Prerequisites: Node.js 20+, npm 9+

1. **Navigate to frontend directory:**
```bash
cd frontend
```

2. **Install dependencies:**
```bash
npm install
```

3. **Start development server:**
```bash
npm run dev
```

Notes:
- Backend must run on port 8080 by default. The API base URL is defined in `frontend/services/api.js` (`API_BASE_URL = 'http://localhost:8080'`). Change it there if your backend runs on a different host/port.
- Optional: after building (`npm run build`), you can preview with `npm run preview`.

---
## Granting ADMIN role for testing

If you want to quickly test the admin panel locally (without creating a dedicated endpoint), you can promote any existing user to ADMIN directly in the database.

Table name is `users` and the `role` column stores enum values as strings (`USER`, `ADMIN`). You may also want to enable the account if it is not verified yet.

### With Docker

```bash
# Connect to PostgreSQL container
docker exec -it hotel_db psql -U postgres -d hotel_db

# Run SQL commands
```

```sql
-- Promote by email
UPDATE users
SET role = 'ADMIN', enabled = TRUE
WHERE email = 'tester@example.com';

-- Or promote by ID
UPDATE users
SET role = 'ADMIN', enabled = TRUE
WHERE id = 1;
```

### Manual Installation

```bash
psql -U postgres -d hotel_db
```

```sql
-- Promote by email
UPDATE users
SET role = 'ADMIN', enabled = TRUE
WHERE email = 'tester@example.com';

-- Or promote by ID
UPDATE users
SET role = 'ADMIN', enabled = TRUE
WHERE id = 1;
```

Notes:
- Works with H2, PostgreSQL and MySQL when using default schema created by JPA.
- If you use H2 console, enable it and open `/h2-console` (or use your DB client) and execute the query.
- Revert back to a normal user by setting `role = 'USER'`.
---

## 🧪 Testing Backend

Run tests with:
```bash
./mvnw test
```
Includes unit and integration tests.

---

## Author
**Jacek Kozakowski** – [LinkedIn](https://www.linkedin.com/in/jacek-kozakowski/)
