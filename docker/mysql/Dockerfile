FROM mysql:8.4
COPY sql/schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY sql/views.sql /docker-entrypoint-initdb.d/02-views.sql
COPY sql/triggers.sql /docker-entrypoint-initdb.d/03-triggers.sql
COPY sql/procedures.sql /docker-entrypoint-initdb.d/04-procedures.sql
COPY sql/security.sql /docker-entrypoint-initdb.d/05-security.sql
COPY sql/events.sql /docker-entrypoint-initdb.d/06-events.sql
COPY docker/mysql/07-network.sql /docker-entrypoint-initdb.d/07-network.sql
COPY docker/mysql/08-demo.sql /docker-entrypoint-initdb.d/08-demo.sql
