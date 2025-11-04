FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY shared ./shared
COPY server ./server
COPY client/pom.xml ./client/pom.xml
COPY admin/pom.xml ./admin/pom.xml
RUN mvn install -N -DskipTests && \
    mvn clean install -DskipTests -pl shared && \
    mvn clean package -DskipTests -pl server

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/server/target/server-0.1.0-SNAPSHOT.jar /app/server.jar
COPY --from=build /build/server/target/classes/server-config.properties /app/server-config.properties
RUN mkdir -p /app/admin/content/imagesets
EXPOSE 5050
CMD ["java", "-jar", "server.jar"]

