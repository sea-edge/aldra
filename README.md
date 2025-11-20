# Albrecht Drais

Project Template by Spring-boot & MyBatis

## Overview

### aldra-api

RESTful API application (use Spring-boot)

### aldra-batch

Batch application (use Spring Batch)

### aldra-common

Common implementations for aldra-api / aldra-batch

### aldra-database

Database implementation (use MyBatis)

## Requirements

### System / Tools

- Java25
  ```sh
  $ java --version
  openjdk 25 2025-03-18
  OpenJDK Runtime Environment (build 25+36)
  OpenJDK 64-Bit Server VM (build 25+36, mixed mode, sharing)
  ```
- Docker
  ```sh
  $ docker --version
  Docker version 20.10.12, build e91ed57
  ```
- mise
  ```sh
  $ mise --version
  mise 2025.x.y (or newer)
  ```

## Setup

1. Install [mise](https://mise.jdx.dev/) and add `eval "$(mise activate zsh)"` (or bash/fish equivalent) to your shell rc so the toolchain & env vars load automatically inside this repo.
2. Copy the local override template and fill in your secrets.
   ```sh
   cp .mise.local.example .mise.local.toml
   $EDITOR .mise.local.toml   # set DB_* and AWS values used by Gradle & Docker
   ```
3. Install the pinned toolchain and trust the project manifest.
   ```sh
   mise install
   mise trust
   ```
4. Launch middleware
    ```sh
    $ docker compose build
    $ docker compose up -d
    ```
5. Provision the database schema / generators.
   ```sh
   ./gradlew :database:flywayMigrate
   ./gradlew :database:mbGenerate
   ```
