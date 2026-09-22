-- Docker-only adaptation: the CLI connects from another container.
-- Preserve the procedure grants and default roles from sql/security.sql.
-- Compose does not publish a database port to the host.
RENAME USER 'account_accesso'@'localhost' TO 'account_accesso'@'%',
            'account_utente'@'localhost' TO 'account_utente'@'%',
            'account_gestore'@'localhost' TO 'account_gestore'@'%';
