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
import org.junit.Assert;
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
    db.tables =
        om.convertValue(db.connectAndCollectMetadata(), new TypeReference<Map<String, Table>>() {});
    return db;
  }

  @Test
  public void subclass() throws Exception {
    RDF4J db = db(Arrays.asList("/data/meta.n3"));

    // infer a subclass to be a type also
    Assert.assertTrue(db.tables.containsKey("http://ex.org/EMP"));

    // infer a subclass to also inherit properties
    Assert.assertTrue(
        db.tables.get("http://ex.org/INTERN").properties.containsKey("http://ex.org/NAME"));
    Assert.assertTrue(
        db.tables.get("http://ex.org/EMP").properties.containsKey("http://ex.org/NAME"));
    name(db.tables);
  }

  void name(Map<String, Table> meta) {
    Assert.assertEquals("http://ex.org/NAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").name);
    Assert.assertEquals("dj/meta/http:%2F%2Fex.org%2FEMP/http:%2F%2Fex.org%2FNAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").ID);
    Assert.assertEquals("dj/meta/http:%2F%2Fex.org%2FEMP",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").parent);
    Assert.assertEquals("string",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").type);
    Assert.assertEquals("NAME",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/NAME").title);

    Assert.assertEquals("dj/meta/http:%2F%2Fex.org%2FPRJ/ID",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/WORKSON").ref);

    Assert.assertEquals("array",
        meta.get("http://ex.org/EMP").properties.get("http://ex.org/EMAIL").type);
    Assert.assertEquals("string",
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
    Assert.assertEquals("mike",
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"))
            .get("http://ex.org/NAME"));
    Assert.assertEquals("[joe@corp, joe@internal]", getEmail(db));

    // all
    Assert.assertEquals("[joe@corp, joe@internal]",
        db.all(db.tables.get("http://ex.org/EMP"), null, null, null, false, null).get(0)
            .get("http://ex.org/EMAIL").toString());

    // update single value
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", "hans"));
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", "franz"));
    Assert.assertEquals("franz",
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
      Assert.assertEquals(1, count);
    }

    // update multi value
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/EMAIL", Arrays.asList("joe@other")));
    Assert.assertEquals("[joe@other]", getEmail(db));

    // set prop to null
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/EMAIL", null));
    db.update(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"),
        MapUtil.of("http://ex.org/NAME", null));
    Map<String, Object> nullProps =
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assert.assertNull(nullProps.get("http://ex.org/NAME"));
    Assert.assertNull(nullProps.get("http://ex.org/EMAIL"));

    // delete
    db.delete(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assert.assertNull(
        db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1")));

    // create
    db.create(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1",
        "http://ex.org/EMAIL", Arrays.asList("joe@new"), "http://ex.org/NAME", "new"));
    nullProps = db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));
    Assert.assertEquals(3, nullProps.keySet().size());
    Assert.assertEquals("http://ex.org/1", nullProps.get("ID"));
    Assert.assertEquals("new", nullProps.get("http://ex.org/NAME"));
    Assert.assertEquals("[joe@new]", nullProps.get("http://ex.org/EMAIL").toString());
  }

  String getEmail(RDF4J db) throws Exception {
    return db.read(db.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"))
        .get("http://ex.org/EMAIL") + "";
  }

  @Test
  public void testSenseSingleCardinality() throws Exception {
    // ontology does not define the cardinality - sensed data suggests 1 and adds this to onto
    RDF4J db = db(Arrays.asList("/data/cardinality.n3"));

    Assert.assertEquals("string",
        db.tables.get("http://ex.org/EMP").properties.get("http://ex.org/SINGLE").type);
    Assert.assertNull(
        db.tables.get("http://ex.org/EMP").properties.get("http://ex.org/SINGLE").items);
  }
}
