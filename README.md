
# 🏨 Hotel Reservation System

A comprehensive Spring Boot-based hotel reservation system with JWT authentication, email verification, and role-based access control.

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

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL 14+

### Installation
Make sure you have **PostgreSQL** and **psql** installed!

1. **Clone the repository**
```bash
git clone https://github.com/jacek-kozakowski/springboot-hotel-management.git
cd springboot-hotel-management
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

3. **Configure database**

Log in as your superuser:
```bash
psql -U postgres
```

In psql use these commands (change the values if needed):
```sql
CREATE DATABASE hotel_db;
CREATE USER hotel_user WITH PASSWORD 'password123';
ALTER DATABASE hotel_db OWNER TO hotel_user;
\q
```

4. **Set up `application.properties` credentials**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_db
spring.datasource.username=hotel_user
spring.datasource.password=password123

# JWT secret key
jwt.secret=YourSecretKeyHere

# Optional: mail credentials
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

Or you can configure the properties with a `.env` file.

5. **Run the application**

**MacOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```powershell
.\mvnw spring-boot:run
```

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

## 🔌 API Endpoints

### User
- `GET /users/me` - Retrieves current user's data
- `GET /users/get-users/{userId}` - Retrieves specific user's data

### Authentication
- `POST /auth/register` - Register new user  
- `POST /auth/login` - Login with JWT response  
- `POST /auth/verify` - Verify email address  
- `POST /auth/resend` - Resend verification code  

### Rooms
- `GET /rooms/all` - Get all rooms (Admin only)  
- `GET /rooms/room` - Search/filter rooms  
  - Params: `roomNumber`, `type`, `minCapacity`, `maxPricePerNight`, `checkInDate`, `checkOutDate`
- `POST /rooms/create-room` - Add new room (Admin only)  

### Reservations
- `GET /reservations/my-reservations` - Get user's reservations
- `GET /reservations/get-reservations/{userId}` - Retrieve specific user's reservations (Admin only)
- `POST /reserve-room` - Create new reservation  
- `PATCH /confirm-reservation/{reservationId}` - Confirm reservation  
- `PATCH /cancel-reservation/{reservationId}` - Cancel reservation  

## 🧪 Testing

Run tests with:
```bash
./mvnw test
```

Includes:
- Unit tests for services  
- Integration tests for controllers
