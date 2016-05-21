## Set environment variable

windows: set MAVEN_OPTS="-Djava.library.path=target/natives"

unix: export MAVEN_OPTS=-Djava.library.path=target/natives

## Run

mvn compile exec:java -Dexec.mainClass=org.pandcorps.furguardians.FurGuardiansGame

