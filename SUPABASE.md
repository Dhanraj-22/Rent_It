# Supabase database deployment

Supabase hosts the PostgreSQL database; the Spring Boot API runs on a Java-capable host such as Render, Railway, Fly.io, or a VM.

1. In the Supabase dashboard, open **Connect** and copy the **Session Pooler** JDBC connection string. Use port `5432`, not the Transaction Pooler on `6543`, because this application uses JPA/Hibernate.
2. URL-encode reserved characters in the database password before placing it in the URL.
3. Configure the API host with these environment variables:

```text
SPRING_PROFILES_ACTIVE=supabase
SUPABASE_DB_URL=jdbc:postgresql://[POOLER-HOST]:5432/postgres?user=postgres.[PROJECT-REF]&password=[URL-ENCODED-PASSWORD]&sslmode=require
APP_BASE_URL=https://api.example.com
APP_CORS_ALLOWED_ORIGINS=https://app.example.com
PORT=8086
```

`SUPABASE_DB_URL` is intentionally not stored in this repository. The exact pooler hostname must come from the Supabase Connect dialog.

For a direct database connection, use the direct JDBC string from that dialog only when the deployment host supports IPv6 or the project has the IPv4 add-on.
