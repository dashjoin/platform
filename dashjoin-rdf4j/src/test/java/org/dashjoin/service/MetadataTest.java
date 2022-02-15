package org.dashjoin.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataTest {

  private static final ObjectMapper om = new ObjectMapper();

  RDF4J db(List<String> datasets) throws Exception {
    RDF4J db = new RDF4J() {
      @Override
      RepositoryConnection getConnection() throws RepositoryException {
        return _cp.getConnection();
      }
    };
    db.ID = "dj/meta";
    db.datasets = datasets;
    db.mode = "memory";
    db.tables =
        om.convertValue(db.connectAndCollectMetadata(), new TypeReference<Map<String, Table>>() {});
    return db;
  }

  @Test
  public void subclass() throws Exception {
    RDF4J db = db(Arrays.asList("/data/meta.n3"));

    // infer a subclass to be a type also
    Assertions.assertTrue(db.tables.containsKey("http://ex.org/EMP"));

    // infer a subclass to also inherit properties
    Assertions.assertTrue(
        db.tables.get("http://ex.org/INTERN").properties.containsKey("http://ex.org/NAME"));
    Assertions.assertTrue(
        db.tables.get("http://ex.org/EMP").properties.containsKey("http://ex.org/NAME"));
    name(db.tables);
  }

  void name(Map<String, Table> meta) {
    Assertions.assertEquals("http://ex.org/NAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").name);
    Assertions.assertEquals("dj/meta/http:%2F%2Fex.org%2FEMP/http:%2F%2Fex.org%2FNAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").ID);
    Assertions.assertEquals("dj/meta/http:%2F%2Fex.org%2FEMP",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").parent);
    Assertions.assertEquals("string",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").type);
    Assertions.assertEquals("NAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").title);

    Assertions.assertEquals("dj/meta/http:%2F%2Fex.org%2FPRJ/ID",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/WORKSON").ref);

    Assertions.assertEquals("array",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/EMAIL").type);
    Assertions.assertEquals("string",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/EMAIL").items.type);
  }

  @Test
  public void detectProps() throws Exception {
    RDF4J db = db(Arrays.asList("/data/props.n3"));
    name(db.tables);
  }

  @Test
  public void listProp() throws Exception {
    RDF4J db = db(Arrays.asList("/data/props.n3"));

    // read
    Assertions.assertEquals("mike",
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"))
            .get("http://ex.org/NAME"));
    Assertions.assertEquals("[joe@corp, joe@internal]", getEmail(db));

    // all
    Assertions.assertEquals("[joe@corp, joe@internal]",
        db.all(db.tables.get("http://ex.org/EMP"), null, null, null, false, null).get(0)
            .get("http://ex.org/EMAIL").toString());

    // update single value
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", "hans"));
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", "franz"));
    Assertions.assertEquals("franz",
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"))
            .get("http://ex.org/NAME"));
    int count = 0;
    try (RepositoryConnection con = db._cp.getConnection()) {
      try (RepositoryResult<Statement> iter = con.getStatements(db.vf.createIRI("http://ex.org/1"),
          db.vf.createIRI("http://ex.org/NAME"), null)) {
        while (iter.hasNext()) {
          iter.next();
          count++;
        }
      }
      Assertions.assertEquals(1, count);
    }

    // update multi value
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/EMAIL", Arrays.asList("joe@other")));
    Assertions.assertEquals("[joe@other]", getEmail(db));

    // set prop to null
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/EMAIL", null));
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", null));
    Map<String, Object> nullProps =
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assertions.assertNull(nullProps.get("http://ex.org/NAME"));
    Assertions.assertNull(nullProps.get("http://ex.org/EMAIL"));

    // delete
    db.delete(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assertions.assertNull(
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1")));

    // create
    db.create(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1",
        "http://ex.org/EMAIL", Arrays.asList("joe@new"), "http://ex.org/NAME", "new"));
    nullProps = db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assertions.assertEquals(3, nullProps.keySet().size());
    Assertions.assertEquals("http://ex.org/1", nullProps.get("ID"));
    Assertions.assertEquals("new", nullProps.get("http://ex.org/NAME"));
    Assertions.assertEquals("[joe@new]", nullProps.get("http://ex.org/EMAIL").toString());
  }

  String getEmail(RDF4J db) throws Exception {
    return db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"))
        .get("http://ex.org/EMAIL") + "";
  }

  @Test
  public void testSenseSingleCardinality() throws Exception {
    // ontology does not define the cardinality - sensed data suggests 1 and adds this to onto
    RDF4J db = db(Arrays.asList("/data/cardinality.n3"));

    Assertions.assertEquals("string",
        db.tables.get("http://ex.org/EMP").properties.get("http://ex.org/SINGLE").type);
    Assertions.assertNull(
        db.tables.get("http://ex.org/EMP").properties.get("http://ex.org/SINGLE").items);
  }
}
