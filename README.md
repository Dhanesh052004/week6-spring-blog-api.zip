# Blog Management REST API

A production-quality RESTful backend for managing blog **Posts**, **Categories**, and **Comments**, built with Java 17 and Spring Boot 3.

## 1. Description

This API provides complete CRUD functionality for a blog platform, with validation, centralized error handling, pagination and sorting, structured logging, Swagger/OpenAPI documentation, and a full suite of automated tests. It runs against an in-memory H2 database in development and is pre-configured for PostgreSQL in production.

## 2. Features

- Full CRUD for Posts, Categories, and Comments
- Pagination and sorting on all list endpoints
- Bean validation with clear, field-level error messages
- Centralized exception handling with a consistent JSON error format
- DTO-based API (JPA entities are never exposed directly)
- Basic comment moderation logic
- Swagger / OpenAPI 3 interactive documentation
- Structured logging via SLF4J + Logback
- Unit tests (JUnit 5 + Mockito) and full-stack integration tests (MockMvc + H2)
- Postman collection covering every endpoint
- Seeded sample data for local development

## 3. Technology Stack

| Layer          | Technology                              |
|----------------|------------------------------------------|
| Language       | Java 17+                                  |
| Framework      | Spring Boot 3.3.x                         |
| Web            | Spring Web (Spring MVC)                   |
| Persistence    | Spring Data JPA + Hibernate ORM           |
| Dev Database   | H2 (in-memory)                            |
| Prod Database  | PostgreSQL                                |
| Build Tool     | Maven                                     |
| Validation     | Jakarta Bean Validation                   |
| Boilerplate    | Lombok                                    |
| API Docs       | springdoc-openapi (Swagger UI)            |
| Logging        | SLF4J + Logback                           |
| Testing        | JUnit 5, Mockito, Spring Boot Test, MockMvc |

## 4. Architecture

The application follows a classic layered architecture:

```
Controller  →  Service  →  Repository  →  Database
   ↓              ↓
  DTOs        Business logic, validation, transactions
```

- **Controllers** only handle HTTP concerns (routing, status codes, request/response mapping) — no business logic.
- **Services** contain all business logic, transactional boundaries, and orchestration between repositories.
- **Repositories** are thin Spring Data JPA interfaces.
- **DTOs** decouple the public API contract from the internal JPA entity model.
- **GlobalExceptionHandler** centralizes error translation into a consistent JSON shape.

## 5. Project Structure

```
week6-spring-blog-api/
├── src/main/java/com/blogapi/
│   ├── BlogApiApplication.java
│   ├── controller/
│   │   ├── PostController.java
│   │   ├── CategoryController.java
│   │   └── CommentController.java
│   ├── service/
│   │   ├── PostService.java
│   │   ├── CategoryService.java
│   │   └── CommentService.java
│   ├── repository/
│   │   ├── PostRepository.java
│   │   ├── CategoryRepository.java
│   │   └── CommentRepository.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── Post.java
│   │   │   ├── Category.java
│   │   │   └── Comment.java
│   │   └── dto/
│   │       ├── PostRequest.java / PostResponse.java
│   │       ├── CategoryRequest.java / CategoryResponse.java
│   │       ├── CommentRequest.java / CommentResponse.java
│   │       ├── ApiResponse.java
│   │       └── ErrorResponse.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── InvalidRequestException.java
│   │   └── GlobalExceptionHandler.java
│   └── config/
│       ├── SwaggerConfig.java
│       └── DataInitializer.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
├── src/test/java/com/blogapi/
│   ├── BlogApiApplicationTests.java
│   ├── service/ (unit tests)
│   └── controller/ (integration tests)
├── docs/
│   └── postman_collection.json
├── pom.xml
├── README.md
└── .gitignore
```

## 6. Database Design

**Category** (1) ──< **Post** (many) ──< **Comment** (many)

| Entity   | Key Fields                                              |
|----------|-----------------------------------------------------------|
| Post     | id, title, content, author, createdAt, updatedAt, category |
| Category | id, name (unique), description                            |
| Comment  | id, content, author, createdAt, post                       |

- `Category → Post`: `@OneToMany(mappedBy = "category")`, lazy fetch
- `Post → Category`: `@ManyToOne`, lazy fetch, `@JoinColumn(name = "category_id")`
- `Post → Comment`: `@OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)`, lazy fetch
- `Comment → Post`: `@ManyToOne`, lazy fetch, `@JoinColumn(name = "post_id")`
- A category cannot be deleted while posts still reference it (returns `400 Bad Request`).
- Deleting a post cascades and removes its comments.

## 7. API Endpoints

### Posts

| Method | Endpoint                          | Description                        |
|--------|------------------------------------|-------------------------------------|
| GET    | `/api/posts`                       | Paginated list (`?page&size&sort`) |
| GET    | `/api/posts/{id}`                  | Get a single post                  |
| GET    | `/api/posts/category/{categoryId}` | Posts in a category (paginated)    |
| GET    | `/api/posts/author/{author}`       | Posts by author (paginated)        |
| POST   | `/api/posts`                       | Create a post                      |
| PUT    | `/api/posts/{id}`                  | Update a post                      |
| DELETE | `/api/posts/{id}`                  | Delete a post                      |

### Categories

| Method | Endpoint                | Description         |
|--------|---------------------------|----------------------|
| GET    | `/api/categories`         | List all categories  |
| GET    | `/api/categories/{id}`    | Get a category       |
| POST   | `/api/categories`         | Create a category    |
| PUT    | `/api/categories/{id}`    | Update a category    |
| DELETE | `/api/categories/{id}`    | Delete a category    |

### Comments

| Method | Endpoint                          | Description                  |
|--------|------------------------------------|-------------------------------|
| GET    | `/api/posts/{postId}/comments`     | Paginated comments for a post |
| POST   | `/api/posts/{postId}/comments`     | Add a comment to a post       |
| PUT    | `/api/comments/{id}`               | Update a comment              |
| DELETE | `/api/comments/{id}`               | Delete a comment              |

### HTTP Status Codes Used

`200 OK` · `201 Created` · `204 No Content` · `400 Bad Request` · `404 Not Found` · `409/400 Conflict-style validation` · `500 Internal Server Error`

## 8. Installation Requirements

- JDK 17 or later
- Maven 3.8+
- (Production only) PostgreSQL 13+

## 9. Setup Instructions

```bash
cd week6-spring-blog-api
mvn clean install
```

This downloads dependencies and compiles the project.

## 10. Configuration

| Property file                | Purpose                          |
|-------------------------------|-----------------------------------|
| `application.properties`      | Common settings for all profiles |
| `application-dev.properties`  | H2 in-memory DB (default profile) |
| `application-prod.properties` | PostgreSQL, configured via env vars |

Production environment variables:

```bash
export DB_URL=jdbc:postgresql://<host>:5432/blogdb
export DB_USERNAME=<username>
export DB_PASSWORD=<password>
```

## 11. How to Run

**Development (H2, default):**

```bash
mvn spring-boot:run
```

**Production (PostgreSQL):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Run the packaged JAR:**

```bash
mvn clean package
java -jar target/blog-management-api.jar
```

The API starts on **http://localhost:8080**.

## 12. Swagger Documentation

Once running, open:

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- Raw OpenAPI spec: **http://localhost:8080/v3/api-docs**

## 13. H2 Console

Available only in the `dev` profile:

- URL: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:blogdb`
- Username: `sa`
- Password: *(blank)*

## 14. Postman Testing

Import [`docs/postman_collection.json`](docs/postman_collection.json) into Postman. It includes requests (with example bodies and example responses) organized into **Posts**, **Categories**, and **Comments** folders, using a `baseUrl` collection variable (defaults to `http://localhost:8080`).

## 15. Sample API Requests

**Create a category:**

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Technology", "description": "Posts about technology trends"}'
```

**Create a post:**

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title": "Getting Started with Spring Boot", "content": "Spring Boot makes backend development easier...", "author": "John Doe", "categoryId": 1}'
```

**Paginated posts, newest first:**

```bash
curl "http://localhost:8080/api/posts?page=0&size=10&sort=createdAt,desc"
```

**Add a comment:**

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"content": "Great post!", "author": "Jane Smith"}'
```

## 16. Sample Data

On startup, the `dev` profile seeds:

- 3 categories: **Technology**, **Programming**, **Web Development**
- 5 blog posts across those categories
- Several comments distributed across the posts

## 17. Running Tests

```bash
mvn test
```

This runs:

- **Unit tests** (`src/test/java/com/blogapi/service`) — service-layer logic against mocked repositories (Mockito), covering successful create/update/delete, not-found scenarios, invalid category references, pagination, and moderation validation.
- **Integration tests** (`src/test/java/com/blogapi/controller`) — full request → controller → service → repository → H2 flows via `MockMvc`, covering CRUD, validation errors, and business-rule errors (e.g. deleting a category that still has posts).

## 18. Screenshots

*(Add screenshots of Swagger UI, a sample API response, and the H2 console here once the application is running locally.)*

## 19. Future Enhancements

- Authentication & authorization (Spring Security + JWT)
- Full-text search on posts
- Rate limiting on comment creation
- Soft delete / audit trail
- Flyway or Liquibase for versioned schema migrations
- Caching frequently-read endpoints (e.g. category list)
- Dockerfile and docker-compose for one-command local startup
