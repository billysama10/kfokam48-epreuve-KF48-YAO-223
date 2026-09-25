# Étape 1 : compilation avec le wrapper Maven commité
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package

# Étape 2 : exécution
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/presences-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
