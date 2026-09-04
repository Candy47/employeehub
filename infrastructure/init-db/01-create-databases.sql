-- Runs automatically ONLY on a fresh Postgres data volume
-- (Docker executes files in /docker-entrypoint-initdb.d on first init).
--
-- POSTGRES_DB in docker-compose already creates `auth_db`.
-- Here we add the second per-service database used by employee-service.
--
-- If your volume already exists, create it manually instead:
--   docker exec -it employeehub-postgres psql -U postgres -c "CREATE DATABASE employee_db;"

CREATE DATABASE employee_db;
CREATE DATABASE audit_db;



