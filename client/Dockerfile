FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY shared ./shared
COPY client ./client
COPY server/pom.xml ./server/pom.xml
COPY admin/pom.xml ./admin/pom.xml
RUN mvn install -N -DskipTests && \
    mvn clean install -DskipTests -pl shared && \
    mvn clean package -DskipTests -pl client

FROM eclipse-temurin:17-jdk
WORKDIR /app
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libxtst6 \
    xvfb \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/client/target/client-0.1.0-SNAPSHOT.jar /app/client.jar
COPY --from=build /build/client/target/classes /app/classes
ENV DISPLAY=:99
CMD Xvfb :99 -screen 0 1024x768x16 & java -jar client.jar

