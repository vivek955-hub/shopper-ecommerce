# What changed (Steps 1–6)

## Step 1 – Foundation
- `pom.xml`: stable Spring Boot 3.5.6 (was a SNAPSHOT), snapshot repos removed, MySQL driver, validation and actuator added
- Config via environment (`SPRING_DATASOURCE_*`), H2 default, `open-in-view` off
- Backend Dockerfile fixed (Java 21, multi-stage, non-root); `docker-compose.yml` paths fixed and MySQL added
- Product list no longer sends image bytes (DTOs + projection queries)

## Step 2 – Orders
- New `CustomerOrder` / `OrderItem` entities, `POST /api/orders`, `GET /api/orders[/{id}]`
- Transactional, atomic stock reservation with price snapshot
- Frontend checkout now makes one API call (the old per-item PUT loop is gone)

## Step 3 – API quality
- Request validation, global error handler, proper 404/400/409
- Pagination + category filter + keyword search, safe update (id from URL, optional image)
- Constructor injection, CORS restricted by config

## Step 4 – Tests
- Repository, service, controller and integration tests (incl. concurrent-buyer test)

## Step 5 – Frontend clean-up
- One axios instance + `VITE_API_URL` (no hardcoded localhost), no blob image fetching, commented-out code removed
- Pagination UI, debounced search, error messages from the API, category "All" option, cart quantity persisted, category-case bug fixed, duplicate `AppProvider` removed

## Step 6 – Docs & delivery
- README rewritten, `.env.example`, `.gitignore`, frontend Dockerfile + nginx, GitHub Actions CI
