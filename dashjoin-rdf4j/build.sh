# make sure dashjoin-sdk is built
cd ..
mvn install
cd dashjoin-rdf4j

mvn install -Denforcer.skip
docker build -t dashjoin/rdf4j .
docker push dashjoin/rdf4j:latest
docker tag dashjoin/rdf4j:latest gcr.io/djfire-1946d/rdf4j
docker push gcr.io/djfire-1946d/rdf4j

# also publish the container under the release version name (in addition to latest)
docker tag dashjoin/rdf4j:latest dashjoin/rdf4j:2.5.3
docker push dashjoin/rdf4j:2.5.3

docker tag dashjoin/rdf4j:latest gcr.io/djfire-1946d/rdf4j:2.5.3
docker push gcr.io/djfire-1946d/rdf4j:2.5.3

# on gcr, can use the UI
