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

- Java21
  ```sh
  $ java --version
  openjdk 21 2023-09-19
  OpenJDK Runtime Environment (build 21+35)
  OpenJDK 64-Bit Server VM (build 21+35, mixed mode, sharing)
  ```
- Docker
  ```sh
  $ docker --version
  Docker version 20.10.12, build e91ed57
  ```
- direnv
  ```sh
  $ direnv --version
  2.28.0
  ```

## Setup

### Activate direnv

```sh
$ cp .envrc.origin .envrc
$ direnv allow
```

### Launch middleware

```sh
$ docker compose build
$ docker compose up -d
```
