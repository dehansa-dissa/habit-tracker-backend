# HabitFlow — Backend

A secure, multi-user habit tracking REST API built with Spring Boot and integrated with Asgardeo for identity and access management.

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Security 6 + OAuth2 Resource Server**
- **JWT Authentication via Asgardeo**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **Maven**
- **Lombok**

## 🔐 Security

- All endpoints protected with JWT Bearer token validation
- JWT tokens issued and managed by **Asgardeo** (WSO2 Identity Platform)
- Token validated against Asgardeo's JWKS endpoint
- User email extracted from JWT claims for multi-user data isolation
- Each user can only access their own habits — enforced at service layer

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/habits` | Get all habits for authenticated user |
| POST | `/api/habits` | Create a new habit |
| DELETE | `/api/habits/{id}` | Delete a habit |
| POST | `/api/habits/{id}/complete` | Mark habit complete for today |
| GET | `/api/habits/{id}/completions` | Get all completions for a habit |
| GET | `/actuator/health` | Health check (public) |

All endpoints except `/actuator/health` require: Authorization: Bearer <Asgardeo JWT Token>


Authorization: Bearer <Asgardeo JWT Token>
