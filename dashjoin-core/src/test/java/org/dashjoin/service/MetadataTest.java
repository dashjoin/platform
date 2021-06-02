package org.dashjoin.service;

import static org.dashjoin.service.QueryEditor.Col.col;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.inject.Inject;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.Col;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.junit.QuarkusTest;

/**
 * tests SQL / JDBC metadata
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class MetadataTest {

  @Inject
  Services services;

  @BeforeAll
  @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"},
      justification = "unit test only")
  public void before() throws Exception {
    // services.persistantDB = new JSONFileDatabase();
    // services.readonlyDB = new JSONClassloaderDatabase();
    services.getConfig().metadataCollection();
    db = (SQLDatabase) services.getConfig().getDatabase("dj/junit");
  }

  static SQLDatabase db;

  @Test
  public void testComposite() throws Exception {
    String url = "jdbc:h2:mem:comp";
    try (Connection con = DriverManager.getConnection(url)) {
      try (Statement s = con.createStatement()) {
        s.execute("create table t(a int, b int, primary key (a,b))");
        s.execute(
            "create table s(i int primary key, x int, y int, foreign key (x,y) references t(a,b))");
        Assert.assertEquals("{S=Table S {pk=[I], fk={T=[X, Y]}}, T=Table T {pk=[A, B], fk={}}}",
            new Metadata(con, url).toString());
      }
    }
  }

  @Test
  public void testGetFkRef() throws Exception {
    Col c = col("EMP", "WORKSON");
    Col res = db.getFkRef(c);
    Assert.assertEquals("PRJ.ID", res.toString());

    c = col("EMP", "ID");
    res = db.getFkRef(c);
    Assert.assertEquals(null, res);

    c = col("NOKEY", "NAME");
    res = db.getFkRef(c);
    Assert.assertEquals(null, res);
  }

  @Test
  public void testGetPk() throws Exception {
    Col res = db.getPk("PRJ");
    Assert.assertEquals("PRJ.ID", res.toString());

    res = db.getPk("NOKEY");
    Assert.assertEquals(null, res);
  }

  @Test
  public void testGetFk() throws Exception {
    Assert.assertEquals("EMP.WORKSON", db.getFk("PRJ", "EMP").toString());
    Assert.assertEquals(null, db.getFk("EMP", "PRJ"));
    Assert.assertEquals(null, db.getFk("EMP", "NOKEY"));
    Assert.assertEquals(null, db.getFk("NOKEY", "EMP"));
  }

  @Test
  public void testGetTables() throws Exception {
    Table emp = db.tables.get("EMP");
    Assert.assertEquals("EMP", emp.name);
    Assert.assertEquals(0, (int) emp.properties.get("ID").pkpos);
    Assert.assertEquals("dj/junit/PRJ/ID", emp.properties.get("WORKSON").ref);
  }
}
