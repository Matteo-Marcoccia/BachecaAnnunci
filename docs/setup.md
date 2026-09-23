# 🛠️ Manual setup

[Back to the project overview](../README.md)

All paths and commands below are relative to the repository root. This guide configures MySQL and the Java demonstration client without Docker.

## 🛠️ Manual installation requirements

- JDK **17 or later**; Maven compiles for Java 17.
- Apache Maven.
- MySQL Server **8.0+** and a SQL client such as MySQL Workbench or the `mysql` command-line client.
- A local MySQL administrator account for initial setup.

The application uses MySQL; it has no in-memory substitute. Notifications are simulated; no email, SMS, or telephone service is contacted.

## 🚀 Manual installation and launch

### 1. Initialize a fresh database

Open a MySQL administrator session and execute the following files in order:

1. `sql/schema.sql`
2. `sql/views.sql`
3. `sql/triggers.sql`
4. `sql/procedures.sql`
5. `sql/security.sql`
6. `sql/events.sql` (optional for trying the application; required for scheduled message cleanup)

For example, start `mysql -u root -p` from the repository root, then run:

```text
SOURCE sql/schema.sql;
SOURCE sql/views.sql;
SOURCE sql/triggers.sql;
SOURCE sql/procedures.sql;
SOURCE sql/security.sql;
-- Optional cleanup scheduler:
SOURCE sql/events.sql;
```

The schema creates `BachecaAnnunci` and is intended for a fresh installation; it is not a migration script. Do not rerun it over an existing installation. The event script enables the server-wide event scheduler and requires administrative privileges; ensure scheduling is also enabled after server restarts if you need continuous cleanup.

`security.sql` creates three local technical accounts, grants their roles, and sets those roles as defaults. The password literals in that file are **local setup examples**: choose your own passwords before executing it and keep personal credentials out of commits. `CREATE USER IF NOT EXISTS` does not change passwords of existing accounts.

### 2. Configure the environment

The application reads these environment variables:

| Variable | Value |
| --- | --- |
| `DB_URL` | Optional JDBC URL; defaults to `jdbc:mysql://localhost:3306/BachecaAnnunci?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=false&serverTimezone=Europe/Rome` |
| `DB_ACCESS_USER` | `account_accesso` |
| `DB_ACCESS_PASSWORD` | Password assigned to the access account |
| `DB_USER_USER` | `account_utente` |
| `DB_USER_PASSWORD` | Password assigned to the ordinary user account |
| `DB_MANAGER_USER` | `account_gestore` |
| `DB_MANAGER_PASSWORD` | Password assigned to the manager account |

These are database accounts, separate from the usernames registered inside the application. Keep the three technical account names as shown: the profile-verification procedure checks those names.

In PowerShell, configure the current terminal session before launching:

```powershell
$env:DB_ACCESS_USER = 'account_accesso'
$env:DB_ACCESS_PASSWORD = '<your access account password>'
$env:DB_USER_USER = 'account_utente'
$env:DB_USER_PASSWORD = '<your ordinary account password>'
$env:DB_MANAGER_USER = 'account_gestore'
$env:DB_MANAGER_PASSWORD = '<your manager account password>'
```

Replace the password placeholders with the values configured in MySQL. If using an IDE, configure these variables in its run configuration instead. The app does not load `.env` files automatically.

### 3. Build and start

From the repository root:

```shell
mvn verify
mvn org.apache.maven.plugins:maven-dependency-plugin:3.7.0:copy-dependencies -DincludeScope=runtime
```

Run on Windows:

```powershell
java -cp "target/classes;target/dependency/*" app.Main
```

Run on Linux or macOS:

```bash
java -cp 'target/classes:target/dependency/*' app.Main
```

Alternatively, import the Maven project into an IDE, select JDK 17 or later, configure the environment variables above, and run `app.Main`.

### 4. Create the first manager and categories

The manual installation starts without predefined users or categories (the Docker demo already includes them). All registrations create ordinary users.

1. Select **Registrazione** and register a user named `gestore_demo`, choosing a password and using fictitious personal data. Supply at least one contact and a 16-character uppercase alphanumeric fiscal code.
2. In a separate MySQL administrator session, promote that registered user:

```sql
UPDATE BachecaAnnunci.Utente
SET IsGestore = TRUE
WHERE Username = 'gestore_demo';
```

3. Log in again as `gestore_demo` to load the new role.
4. Create a category from the manager menu. Users can now publish listings in it.

The promotion is a one-time administrator setup action; ordinary application accounts cannot promote themselves.

