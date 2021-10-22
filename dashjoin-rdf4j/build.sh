# make sure dashjoin-sdk is built
cd ..
mvn install
cd dashjoin-rdf4j

mvn install -Denforcer.skip
docker build -t dashjoin/rdf4j .
docker tag dashjoin/rdf4j:latest gcr.io/djfire-1946d/rdf4j
docker push gcr.io/djfire-1946d/rdf4j
