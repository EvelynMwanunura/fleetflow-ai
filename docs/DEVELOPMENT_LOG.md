# Development Log

## 2026-11-22 — PostgreSQL persistence and Flyway baseline

- PostgreSQL 19 is the local target at `localhost:5432`, database `fleetflow`. Installation and database provisioning were not verified; no PostgreSQL connection was attempted.
- Kept the PostgreSQL JDBC driver and added Spring Boot Flyway plus the PostgreSQL Flyway database module.
- Added the initial `vehicles` table migration.
- Configured the default application profile for PostgreSQL, Flyway, and Hibernate schema validation. Credentials are supplied through environment variables.
- Added an H2 PostgreSQL-compatibility test profile; Flyway successfully applied the migration and Hibernate validation passed.
- Tests: `.\mvnw.cmd test` — 23 passed, 0 failures, 0 errors; build succeeded.
