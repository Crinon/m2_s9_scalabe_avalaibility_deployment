#TODO archi
/api
nettoyer descrption pom etc
-dockeriser avec BDD propre
-un script de création/destruction de la base de données ou de peuplement de la base si
vous utilisez H2, les fichiers Docker si vous utilisez Docker,

un document expliquant comment vous avez géré les différents points pris en compte pour
l’évaluation (réponse aux besoins, conception, réalisation, sécurité).


# Dev : config intellij pour connexion H2 : jdbc:h2:file:./database/myownrevolutdatabase
# Dev : mvn spring-boot:run 

Gateway : démarrer consul
docker run -p 8500:8500 -p 8600:8600/udp --name=myconsul consul agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0
Lancer le package mygateway

Port 8082 = bank
Port 8080 = gateway
Port 8100 = shop
Port 8500 = consul