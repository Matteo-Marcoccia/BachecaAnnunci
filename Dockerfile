FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B verify org.apache.maven.plugins:maven-dependency-plugin:3.7.0:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./lib
USER 10001:10001
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-cp", "/app/classes:/app/lib/*", "app.Main"]
