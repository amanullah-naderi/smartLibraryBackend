FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/quarkus-app/lib/ ./lib/
COPY build/quarkus-app/*.jar ./
COPY build/quarkus-app/app/ ./app/
COPY build/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8082

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
