FROM eclipse-temurin:25-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl unzip graphviz fontconfig && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /usr/share/fonts/open-sans
COPY open-sans.zip .
RUN unzip open-sans.zip && \
    rm -f open-sans.zip && \
    fc-cache -fv

WORKDIR /usr/local/lib/schemaspy
RUN curl -Lo schemaspy.jar https://github.com/schemaspy/schemaspy/releases/download/v6.2.4/schemaspy-6.2.4.jar

WORKDIR /drivers
RUN curl -JLO https://jdbc.postgresql.org/download/postgresql-42.7.4.jar

WORKDIR /output

WORKDIR /
