# Compilation src vers jar
FROM maven:3-openjdk-17 AS BUILDER
# COPY des pom src et database
COPY pom.xml .
COPY /src ./src
COPY /database ./database
RUN mvn -e -B -DskipTests clean package

FROM openjdk:17-oracle
# Copie du jar à la racine du WORKDIR
COPY --from=BUILDER /target/bank-service-1.0.jar /target/bank-service-1.0.jar
EXPOSE 8082
# Exécution du jar
CMD java -jar /target/bank-service-1.0.jar 


# Créer l'image
# docker build . -t banknico

# Démarrer le conteneur
# docker run --name banque -p 8082:8082 banknico