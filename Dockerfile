FROM maven:3-openjdk-11 AS BUILDER
WORKDIR /app
COPY . .
RUN mvn -e -B -DskipTests clean package

FROM openjdk:11
COPY --from=BUILDER /app/target/myownrevolut-1.0.jar .
EXPOSE 8082
CMD ["java","-jar","myownrevolut-1.0.jar"]
