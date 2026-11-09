# NoteVault

A lightweight, secure Notes Management application with JWT authentication, built with **Java 21** and **Spring Boot 3**.

---

## Tech Stack

- **Backend:** Java 21 LTS, Spring Boot 3.3.4 (Spring Web, Spring Security 6, Spring Data JPA)
- **Database:** MySQL 8.0
- **Security:** Stateless JWT, BCrypt password hashing
- **Frontend:** Vanilla HTML5 / Modern JS + Tailwind CSS  
- **Docs & Testing:** Swagger UI, Postman collection

---

## Features

- **Authentication:** User registration & login with signed JWT tokens.
- **Data Privacy:** Users can only view, create, edit, search, and delete **their own notes**.
- **Search:** Case-insensitive search by note title.
- **Validation & Errors:** Jakarta Bean Validation with custom 404/500 error pages for browser and JSON responses for API clients.

---

## Quick Start

### Prerequisites
- Java 21 LTS or higher
- MySQL Server 8.0

Ensure MySQL is running on port `3306`, configure credentials in `src/main/resources/application.properties`, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

- **Web App:** [http://localhost:8080/](http://localhost:8080/)
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login & receive JWT |
| `POST` | `/api/notes` | Create a note |
| `GET` | `/api/notes` | Get all user notes |
| `GET` | `/api/notes?search=keyword` | Search notes by title |
| `GET` | `/api/notes/{id}` | Get note by ID |
| `PUT` | `/api/notes/{id}` | Update a note |
| `DELETE` | `/api/notes/{id}` | Delete a note |

---

## Repository

- **GitHub:** [https://github.com/ananikets18/NoteVault---A-Way-to-Protect-your-Notes..git](https://github.com/ananikets18/NoteVault---A-Way-to-Protect-your-Notes..git)
