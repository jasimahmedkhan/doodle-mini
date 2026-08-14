FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests clean package

FROM eclipse-temurin:25-jre-noble AS runtime

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system minidoodle \
    && useradd --system --gid minidoodle --home-dir /app minidoodle

WORKDIR /app

COPY --from=build --chown=minidoodle:minidoodle /workspace/target/*.jar app.jar

USER minidoodle

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=5 \
    CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
