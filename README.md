Om dit project te runnen moet allereerst bekend zijn waar de ALPN boot jar staat, deze is nodig voor TLS ALPN.

Om dit te doen, voer in deze map 'gradlew.bat dependencies' uit op windows, of './gradlew dependencies' op linux. Het programma geeft ergens bovenaan een 'ALPN boot argument' lijn weer. Kopieer alles achter de eerste dubbele punt

Vervolgens, ga naar de 'run' map met de terminal
Voer hier het volgende commando uit:

java -Xbootclasspath... -jar server.jar

Vervang '-Xbootclasspath...' door de gekopieerde ALPN boot regel
