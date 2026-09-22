# 📋 Bacheca elettronica di annunci

Academic database project by **Matteo Marcoccia**, developed for the **Basi di Dati** course, academic year **2025–2026**.

A Java command-line application for publishing second-hand listings, exchanging public comments and private messages, following listings, and reporting sales statistics. The interface and project report are in Italian.

## 📌 Features

- Registration and login with salted PBKDF2-HMAC-SHA-256 password hashes.
- Listings with title, description, price, category, and sale status.
- Category browsing including subcategories through a recursive SQL query.
- Listing updates and additional notes restricted to the author.
- Public comments and private conversations between sellers and interested users.
- Following and unfollowing listings, with simulated notifications printed to the console.
- Manager tools for creating hierarchical categories and viewing each user's percentage of sold listings.
- Sold listings disappear from search. Existing conversations can continue; new conversations and comments are blocked.

## 🏗️ Database and application design

The implementation includes **7 tables, 3 views, 23 stored procedures, 14 triggers, and 1 scheduled event**. InnoDB transactions and row locks coordinate selected operations. The event removes private messages older than three years once a month.

Java separates console views, controllers, services, DAO classes, models, and session management. DAOs call stored procedures through JDBC. MySQL roles restrict technical accounts to the procedures they need, without granting direct table access.

| Directory | Contents |
| --- | --- |
| `src/main/java` | Java CLI and JDBC integration |
| `sql` | Schema, views, procedures, triggers, roles, and event |
| `docs` | Requirements, E-R diagrams, relational design, and physical design |

## 🛠️ Requirements

- JDK **17 or later**; Maven compiles for Java 17.
- Apache Maven.
- MySQL Server **8.0+** and a SQL client such as MySQL Workbench or the `mysql` command-line client.
- A local MySQL administrator account for initial setup.

MySQL is required: this project has no in-memory demo. Notifications are simulated; no email, SMS, or telephone service is contacted.

## 🚀 Install and run

### 1. Initialize a fresh database

Open a MySQL administrator session and execute the following files in order:

1. `sql/schema.sql`
2. `sql/views.sql`
3. `sql/triggers.sql`
4. `sql/procedures.sql`
5. `sql/security.sql`
6. `sql/events.sql` (optional for trying the application; required for scheduled message cleanup)

For example, start `mysql -u root -p` from the repository root, then run:

```sql
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

The application starts without predefined users or categories. All registrations create ordinary users.

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

## 🧪 Verification walkthrough

The repository currently has no automated test suite. `mvn verify` checks compilation and packaging; it does not validate a running MySQL installation.

To exercise the main flows on a disposable database:

1. Register a manager and two ordinary users with different usernames and fiscal codes.
2. Create a parent category and a subcategory as the manager.
3. Publish a listing as the first ordinary user.
4. As the second user, find it through the parent category, follow it, add a comment, and send a private message.
5. As the author, reply, add a note, and modify the listing; verify the simulated notifications.
6. Mark the listing as sold. Check that search excludes it, new comments are rejected, and the existing conversation remains usable.
7. Open the manager report and check that a user with one published and sold listing has a 100% sale rate. An ordinary user should not have manager operations.

## 🔐 Scope of the security model

This is an academic local-client application. Java authenticates the application user and passes that username to SQL procedures; the database checks the role and the supplied ownership information. The shared technical database credentials are available to the client, so these checks do not independently authenticate each end user against a modified client or direct SQL access. A production deployment would require a different trust boundary, such as a server handling authentication and retaining database credentials.

## 📄 Documentation

See the [project report](docs/Bacheca%20Elettronica%20Di%20Annunci.pdf) for the requirements, conceptual E-R model, relational schema, workload estimates, normalization, indexes, and SQL design. Workload volumes in the report are design assumptions, not measured benchmark results.
