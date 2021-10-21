mvn install -Denforcer.skip
docker build -t dashjoin/rdf4j .
docker tag dashjoin/rdf4j:latest gcr.io/djfire-1946d/rdf4j
docker push gcr.io/djfire-1946d/rdf4j
