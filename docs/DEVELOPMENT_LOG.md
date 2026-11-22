
# Development Log

## 2026-11-22 — PostgreSQL persistence and Flyway baseline

- PostgreSQL 19 is installed and running locally at `localhost:5432`.
- Created and verified the `fleetflow` database.
- Kept the PostgreSQL JDBC driver and added Spring Boot Flyway plus the PostgreSQL Flyway database module.
- Added the initial `vehicles` table migration.
- Configured the default application profile for PostgreSQL, Flyway, and Hibernate schema validation. Credentials are supplied through environment variables.
- Added an H2 PostgreSQL-compatibility test profile.
- Verified that Flyway successfully applied migration V1 and Hibernate schema validation passed.
- Verified the `vehicles` table and Flyway migration history in PostgreSQL.
- Tests: `.\mvnw.cmd test` — 23 passed, 0 failures, 0 errors; build succeeded.

## 2026-11-22 — Vehicle Management backend

- Implemented the Vehicle entity, repository, service, and REST controller.
- Added CRUD endpoints under `/api/v1/vehicles`.
- Added vehicle registration normalization and duplicate-registration checks.
- Added custom vehicle year validation, approved vehicle type and status validation, and nonnegative checks for optional capacity and mileage.
- Added centralized exception handling for validation errors (400), missing vehicles (404), and duplicate registrations (409).
- Verified API operations manually: create (201), retrieve, update, and delete (204).
- Verified duplicate registration returns HTTP 409.
- Verified invalid vehicle data returns HTTP 400.
- Verified requests for nonexistent vehicles return HTTP 404.
- Added service unit tests and controller tests.
- Automated test results: 23 passed, 0 failures, 0 errors, 0 skipped.
