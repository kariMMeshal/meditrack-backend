# MediTrack Backend

MediTrack is a Spring Boot-based backend system designed to manage hospital biomedical equipment, maintenance operations, and user roles with secure authentication using JWT.

---

## 🚀 Project Overview

The system provides a backend for managing:
- Medical devices inventory
- Maintenance logs and tracking
- User management with role-based access control
- Authentication and authorization using JWT

The project is designed with scalability and modular architecture in mind, making it suitable for hospital or biomedical department environments.

---

## 🧱 Core Features

### 🔐 Authentication & Security
- JWT-based authentication
- Role-based authorization (Admin, User, Biomed)
- Secure password storage using hashing
- Stateless session management

### 👥 User Management
- Admin user is automatically created on startup (if not exists)
- Role assignment system (ROLE_ADMIN, ROLE_USER, ROLE_BIOMED)

### 🏥 Equipment Management
- Store and manage medical devices information
- Track device status and condition

### 🛠 Maintenance Tracking
- Log maintenance activities
- Assign maintenance tasks to users
- Track performed operations per device

---

## ⚙️ System Initialization

On application startup:
- Required roles are created automatically if missing
- Default admin user is seeded from environment configuration (if not already present)

---

## 🛡 Security Model

- Stateless authentication using JWT
- Each request is validated via token filter
- Role-based access control for endpoints
- Secure password encryption using BCrypt

---

## 🌱 Environment Configuration

The project uses environment variables for sensitive configuration:

- Database credentials
- JWT secret and expiration
- Admin account initialization
- External service URLs

A `.env` file is used for local development (not committed to version control).

---

## 🧪 Tech Stack

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- JWT (JSON Web Token)

---

## 📦 Future Improvements

- Refresh token implementation
- Audit logging system
- Advanced analytics dashboard
- Docker support for deployment
- Enhanced monitoring & metrics

---
## 📌 API Documentation

API endpoints and detailed usage are documented separately.

👉 [https://www.notion.so/MediTrack-Backend-API-Docs-4dd1978d13d2439e873849b50810514f?source=copy_link]

---

## 👨‍💻 Developer Notes

This project is currently in active development and focuses on building a strong backend foundation before frontend integration and deployment.

---
