# ShoppeR – Full-Stack E-Commerce App

Spring Boot REST API + React (Vite) storefront with MySQL, transactional orders, validation, pagination, tests and Docker.

## Features
- Product catalogue: create, edit, delete, image upload, category filter, keyword search, pagination
- Shopping cart in the browser (survives refresh); prices and stock are re-checked by the server at checkout
- **Order placement API**: stock is checked and reserved atomically in one database transaction, so an order either fully succeeds or changes nothing, and two buyers can never oversell the last unit
- Orders store a snapshot of product name and price, so later edits or deletions never change past orders
- Consistent JSON errors (400 validation with per-field messages, 404, 409 not enough stock)

## Tech stack
Java 21, Spring Boot 3.5, Spring Data JPA/Hibernate, MySQL 8 (H2 for local dev and tests), Bean Validation, JUnit 5, Mockito, MockMvc, React 18, Vite, Bootstrap, Axios, Docker Compose, Nginx, GitHub Actions.

## Run it

### Option 1 – Docker (MySQL + backend + frontend)
```bash
docker compose up --build
```
Open http://localhost:3000. API: http://localhost:8080/api/products. Demo products are inserted on first start (`APP_SEED_ENABLED`). Copy `.env.example` to `.env` to change passwords.

### Option 2 – Local development (no database install needed)
```bash
# terminal 1 – backend on :8080 (in-memory H2; set APP_SEED_ENABLED=true for demo data)
cd ecom-project
APP_SEED_ENABLED=true ./mvnw spring-boot:run

# terminal 2 – frontend on :5173
cd ecom-frontend-5-main
npm install
npm run dev
```
Windows PowerShell: `$env:APP_SEED_ENABLED="true"; .\mvnw spring-boot:run`

### Tests
```bash
cd ecom-project && ./mvnw test
```
Covers repository queries, service logic (Mockito), controllers/validation/error responses (MockMvc), and an end-to-end order flow including a concurrent-buyers test.

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/products?keyword=&category=&page=0&size=12` | Paged list, newest first (size max 50) |
| GET | `/api/product/{id}` | One product |
| POST | `/api/product` | Create (multipart: `product` JSON part, optional `imageFile`) |
| PUT | `/api/product/{id}` | Update (image optional; keeps the old one if omitted) |
| DELETE | `/api/product/{id}` | Delete (204) |
| GET | `/api/product/{id}/image` | Image bytes |
| POST | `/api/orders` | Place order `{"items":[{"productId":1,"quantity":2}]}` -> 201, or 409 if not enough stock |
| GET | `/api/orders?page=&size=` | Paged orders |
| GET | `/api/orders/{id}` | One order |
| GET | `/actuator/health` | Health check |

Error shape: `{"timestamp","status","error","message","errors":{"field":"message"}}`

## Design decisions
- **Atomic stock update**: `UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty` lets the database refuse to go below zero, instead of read-then-write in Java (which races).
- **Stable lock order**: items are processed sorted by product id, so concurrent orders cannot deadlock each other.
- **Lightweight lists**: list/search queries project into a DTO and never load the image `BLOB`; images are fetched separately and cached by the browser.
- **Config by environment**: H2 by default, MySQL through `SPRING_DATASOURCE_*` variables; CORS origins via `APP_CORS_ALLOWED_ORIGINS`.

## Known limitations / next steps
- No authentication or user accounts (anyone can add/delete products and see all orders)
- The cart lives in the browser; there is no server-side cart or payment
- Schema is managed by `ddl-auto=update`; a migration tool (Flyway) would be the next step for production
- Keyword search uses `LIKE '%term%'`, which does not use an index; fine for small catalogues, use full-text search for large ones
- Images are stored in the database for simplicity; object storage (S3) scales better
- No frontend tests yet

## Deploying
The Docker setup runs on any machine with Docker (a VPS, for example): copy the repo, create `.env` with strong passwords, run `docker compose up -d --build`, and put HTTPS (e.g. Caddy or a cloud load balancer) in front of port 3000. If you host the frontend and API separately, build the frontend with `--build-arg VITE_API_URL=https://your-api/api` and set `APP_CORS_ALLOWED_ORIGINS` on the backend.
