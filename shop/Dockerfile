# Compilation src vers jar
FROM maven:3-openjdk-17 AS BUILDER
# COPY des pom src et database
COPY pom.xml .
COPY /src ./src
RUN mvn -e -B -DskipTests clean package

FROM openjdk:17-oracle
# Copie du jar à la racine du WORKDIR
COPY --from=BUILDER /target/shop-service-1.0.jar /target/shop-service-1.0.jar
EXPOSE 8100
# Exécution du jar
CMD java -jar /target/shop-service-1.0.jar


# Créer l'image
# docker build . -t image_commercant

# Démarrer le conteneur
# docker run --name conteneur_commercant -p 8100:8100 image_commercant