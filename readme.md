# Stinger (serveur de fichiers)
Lancement : ./gradlew quarkusdev <br>
(port 8095)

## Rôle du service
Maintenir une Map de fichiers
1) reçoit une demande de mise en cache d'un fichier
2) met le fichier en cache dans une Map
3) répond avec l'URL d'accès au fichier en cache

## Topics utilisés
STOCKER_FICHIER_DEMANDE<br>
STOCKER_FICHIER_REPONSE<br>

## Diagramme de séquence
Sous Confluence : https://zedreamteam.atlassian.net/wiki/spaces/MASK/pages/164167681/Service%2BKbis%2Bcondor

## Exemple de message 
``
{"entete":{"idUnique":"390c47ca-59db-4075-9c77-c471f63e7cb9","idLot":"12345","dateHeureDemande":"2020-09-24T11:09:55.847341","idEmetteur":"L20057","idGreffe":"0101","typeDemande":"KBIS"},"objetMetier":{"fichierURLAbs":"/tmp/tmp1323478801105707399.pdf","fichierURLRel":"tmp1323478801105707399.pdf","idMetierFichier":"2012B00021"},"reponse":{"estReponseOk":true,"messageErreur":null,"stackTrace":null}}
``
