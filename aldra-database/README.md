# aldra-database

## Setup

Ensure the mise environment exports the database credentials required by Flyway/MyBatis:

```sh
cp .mise.local.example .mise.local.toml   # one-time in repo root
$EDITOR .mise.local.toml                  # DB_JDBC_URL/DB_USER/DB_PASSWORD etc.
mise install && mise trust
```

### Migrate database by Flyway

```sh
$ ./gradlew :database:flywayMigrate
```

### Generate Entity / Mapper classes by Mybatis Generator

```sh
$ ./gradlew :database:mbGenerate
```
