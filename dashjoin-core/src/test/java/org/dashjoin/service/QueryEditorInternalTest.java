package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.QueryEditor.QueryColumn;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QueryEditorInternalTest {

  @Inject
  Services services;

  @Test
  public void joinColumns() throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase("dj/junit");
    QueryResponse res = new QueryResponse();
    res.metadata = new ArrayList<>();
    QueryColumn qc = new QueryColumn();
    qc.col = new QueryEditor.Col();
    qc.col.column = "WORKSON";
    qc.col.table = "EMP";
    qc.keyTable = "PRJ";
    qc.columnID = "dj/junit/EMP/WORKSON";
    res.metadata.add(qc);
    res.joinOptions = new ArrayList<>();
    QueryEditorInternal.joinColumns(db, res, "EMP", null);
    Assert.assertEquals("PRJ.ID", res.joinOptions.get(0).add.toString());
  }

  @Test
  public void tableColumns() throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase("dj/junit");
    QueryResponse res = new QueryResponse();
    res.data = Arrays.asList(MapUtil.of("x", 1));
    res.fieldNames = Arrays.asList("x");
    res.joinOptions = new ArrayList<>();
    QueryEditorInternal.tableColumns(db, res, db.tables.get("EMP"), null);
    Assert.assertEquals("WORKSON", res.joinOptions.get(2).col.column);
  }
}
