package org.dashjoin.service.rdf4j;

import org.apache.commons.lang3.NotImplementedException;
import org.dashjoin.service.ddl.SchemaChange;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * DDL implementation for RDF. Edits are written into the main DB using the urn:ontology context
 */
public class RDF4JSchemaChange implements SchemaChange {

  RDF4J db;
  IRI context;

  public RDF4JSchemaChange(RDF4J db) {
    this.db = db;
    this.context = db.iri("urn:ontology");
  }

  @Override
  public void createTable(String table, String keyName, String keyType) throws Exception {
    try (RepositoryConnection con = db.getConnection()) {
      con.add(db.iri(table), RDF.TYPE, RDFS.CLASS, context);
      con.add(RDFS.LABEL, RDFS.DOMAIN, db.iri(table), context);
      con.add(RDFS.LABEL, RDFS.RANGE, XSD.STRING, context);
      con.add(RDFS.LABEL, OWL.MAXCARDINALITY, db.literal(1), context);
    }
  }

  @Override
  public void dropTable(String table) throws Exception {
    try (RepositoryConnection con = db.getConnection()) {
      Update delete =
          con.prepareUpdate("delete {?s ?p ?o} where {?s a <" + table + "> . ?s ?p ?o}");
      delete.execute();
      con.remove(db.iri(table), RDF.TYPE, RDFS.CLASS, context);
      con.remove(db.iri(table), RDF.TYPE, OWL.CLASS, context);
    }
  }

  @Override
  public void renameTable(String table, String newName) throws Exception {
    throw new NotImplementedException("Rename table is not supported for RDF databases");
  }

  @Override
  public void createColumn(String table, String columnName, String columnType) throws Exception {
    try (RepositoryConnection con = db.getConnection()) {
      con.add(db.iri(columnName), RDFS.DOMAIN, db.iri(table), context);
      con.add(db.iri(columnName), RDFS.RANGE, type(columnType), context);
      con.add(db.iri(columnName), OWL.MAXCARDINALITY, db.literal(1), context);
    }
  }

  IRI type(String columnType) {
    switch (columnType) {
      case "date":
        return XSD.DATE;
      case "integer":
        return XSD.INTEGER;
      case "number":
        return XSD.DOUBLE;
      case "string":
        return XSD.STRING;
      case "boolean":
        return XSD.BOOLEAN;
      default:
        throw new IllegalArgumentException("Unknown type: " + columnType);
    }
  }

  @Override
  public void renameColumn(String table, String column, String newName) throws Exception {
    throw new NotImplementedException("Rename column is not supported for RDF databases");
  }

  @Override
  public void alterColumn(String table, String column, String newType) throws Exception {
    throw new NotImplementedException("Alter column is not supported for RDF databases");
  }

  @Override
  public void dropColumn(String table, String column) throws Exception {
    try (RepositoryConnection con = db.getConnection()) {
      Update delete = con.prepareUpdate(
          "delete {?s ?p ?o} where {?s a <" + table + "> . ?s <" + column + "> ?o. ?s ?p ?o}");
      delete.execute();
      con.remove(db.iri(column), RDFS.DOMAIN, db.iri(table), context);
    }
  }
}
