# spring-boot-starter-celesta

[![build](https://github.com/CourseOrchestra/spring-boot-starter-celesta/actions/workflows/main.yml/badge.svg)](https://github.com/CourseOrchestra/spring-boot-starter-celesta/actions/workflows/main.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.curs/spring-boot-starter-celesta/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ru.curs/spring-boot-starter-celesta)

Spring Boot starter that auto-configures [Celesta](https://courseorchestra.github.io/celesta/en/)
inside a Spring Boot application.

[Celesta](https://courseorchestra.github.io/celesta/en/) is a Java data-access and database-migration
library: you describe your schema once in **CelestaSQL**, Celesta generates type-safe data-access
classes (*cursors*) and keeps the physical database in sync with that description across all supported
RDBMS (PostgreSQL, Oracle, MS SQL Server, H2, Firebird, …).

This starter wires Celesta into the Spring context so that you get:

* a ready-to-use `Celesta` bean, configured from `application.yml`/`application.properties`;
* a connection pool built either from your own `DataSource` bean or from `celesta.jdbc.*` settings;
* declarative transaction management for Celesta procedures via the `@CelestaTransaction` annotation;
* IDE auto-completion for all `celesta.*` configuration properties.

## Requirements

| | Version |
|---------------------|------------------|
| Java                | 17 or newer      |
| Spring Boot         | 3.x              |
| Celesta             | 8.2.x            |

> Need Spring Boot 2.x / Java 11? Use the `3.0.x` line of this starter.

## Getting started

### 1. Add the dependency

Maven:

```xml
<dependency>
    <groupId>ru.curs</groupId>
    <artifactId>spring-boot-starter-celesta</artifactId>
    <version>3.1.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.curs:spring-boot-starter-celesta:3.1.0'
```

(Check the Maven Central badge above for the latest released version.)

### 2. Describe your schema (the *score*)

Put CelestaSQL scripts (`*.sql`) under a *score* directory, e.g. `src/main/resources/score`:

```sql
CREATE SCHEMA demo VERSION '1.0';

CREATE TABLE OrderHeader (
    id        VARCHAR(30) NOT NULL,
    date      DATETIME,
    customer  VARCHAR(50),
    CONSTRAINT pk_OrderHeader PRIMARY KEY (id)
);
```

Add the [`celesta-maven-plugin`](https://courseorchestra.github.io/celesta/en/) to your build to
generate the cursor classes (e.g. `OrderHeaderCursor`) from these scripts, and point the starter at
the score:

```yaml
celesta:
  score-path: classpath:score
```

On application startup Celesta validates the score and automatically migrates the target database to
match it.

### 3. Write a service

Inject the generated cursors (or use a `CallContext` directly) inside a method annotated with
`@CelestaTransaction`:

```java
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.transaction.CelestaTransaction;

@Service
public class OrderService {

    @CelestaTransaction
    public void createOrder(CallContext ctx, String id, String customer) {
        OrderHeaderCursor order = new OrderHeaderCursor(ctx)
            order.setId(id)
                 .setCustomer(customer)
                 .insert();
    }
}
```

The caller supplies a `CallContext` (which carries the current user identity):

```java
CallContext ctx = new CallContext("alice");
//orderService must be a Spring bean
orderService.createOrder(ctx, "ORD-1", "ACME");
```

## The `@CelestaTransaction` annotation

`@CelestaTransaction` marks a method as a Celesta procedure entry point. When the method has a
`CallContext` argument, the starter's aspect:

1. **activates** the `CallContext` against the `Celesta` instance (opening a JDBC connection),
2. invokes the method,
3. **commits** on normal return, or **rolls back** if the method throws,
4. **closes** the `CallContext` in a `finally` block.

Nested `@CelestaTransaction` calls that receive an already-active `CallContext` simply join the
existing transaction instead of starting a new one, so you can compose procedures freely. If the
method has no `CallContext` argument, it is invoked without transaction handling.

## Connection sources

The `Celesta` connection pool is chosen automatically:

* **Your `DataSource` bean** — if a single `javax.sql.DataSource` bean is present in the context
  (e.g. a HikariCP pool configured via `spring.datasource.*`), Celesta uses it. This lets Celesta
  share connections with the rest of your Spring application.
* **`celesta.jdbc.*`** — if no `DataSource` bean exists, Celesta builds its own internal pool from the
  `celesta.jdbc.url` / `username` / `password` settings.
* **In-memory H2** — if neither is provided, Celesta falls back to an in-memory H2 database, which is
  handy for tests and demos.

## Configuration properties

All properties live under the `celesta` prefix. Spring Boot relaxed binding applies, so
`scorePath`, `score-path` and `SCORE_PATH` are equivalent.

| Property                        | Type           | Default | Description |
|---------------------------------|----------------|---------|-------------|
| `celesta.score-path`            | String         | —       | Comma-separated list of score location patterns, e.g. `classpath:score`, `classpath*:score`, `file:///opt/app/score`. |
| `celesta.jdbc.url`              | String         | —       | JDBC connection URL (used when no `DataSource` bean is present). |
| `celesta.jdbc.username`         | String         | —       | JDBC user name. |
| `celesta.jdbc.password`         | String         | —       | JDBC password. |
| `celesta.h2.in-memory`          | boolean        | `false` | Start an in-memory H2 database. |
| `celesta.h2.referential-integrity` | boolean     | `false` | Enable H2 referential integrity. |
| `celesta.h2.port`               | Integer        | —       | Start the H2 web console on the given port. |
| `celesta.skip-db-update`        | boolean        | `false` | Skip database migration on startup. |
| `celesta.force-db-initialize`   | boolean        | `false` | Force database initialization on startup. |
| `celesta.log-logins`            | boolean        | `false` | Log all login and logout actions. |
| `celesta.celesta-scan`          | Set\<String\>  | —       | Packages scanned for Celesta-annotated components. |

### Example `application.yml`

```yaml
celesta:
  score-path: classpath:score
  jdbc:
    url: jdbc:postgresql://localhost:5432/demo
    username: demo
    password: demo
  skip-db-update: false
  log-logins: true
```

## Building from source

```bash
mvn clean verify -P dev
```

The `dev` profile additionally runs Checkstyle and SpotBugs. The build requires JDK 17+.

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
</content>
