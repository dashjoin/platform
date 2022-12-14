package org.dashjoin.util;

import static org.dashjoin.util.MapUtil.of;
import org.dashjoin.function.Invoke;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class OpenAPITest {

  ObjectMapper om = new ObjectMapper(new YAMLFactory());

  @Test
  public void testFunction() throws Exception {
    Invoke x = new Invoke();
    x.ID = "name";
    x.comment = "description";
    Assertions.assertEquals(
        "{/rest/function/name={post={summary=description, operationId=name, requestBody={content={application/json={schema={type=object}}}}, responses={200={content={application/json={schema={}}}, description=name response}}}}}",
        OpenAPI.path(x).toString());
    // System.out.println(om.writeValueAsString(OpenAPI.path(x)));
  }

  @Test
  public void testQuery() throws Exception {
    QueryMeta meta = new QueryMeta();
    meta.ID = "myquery";
    meta.database = "dj/db";
    Assertions.assertEquals(
        "{/rest/database/query/db/myquery={post={operationId=myquery, requestBody={content={application/json={schema={type=object}}}}, responses={200={content={application/json={schema={type=array, items={type=object}}}}, description=myquery response}}}}}",
        OpenAPI.path(meta, null).toString());
    // System.out.println(om.writeValueAsString(OpenAPI.path(meta, null)));
  }

  @Test
  public void testResultMeta() {
    Property p = new Property();
    p.name = "col";
    p.dbType = "VARCHAR";
    p.type = "string";
    Assertions.assertEquals(
        "{content={application/json={schema={type=array, items={type=object, properties={col={type=string, x-dbType=VARCHAR}}}}}}}",
        OpenAPI.resultMeta(of("p", p), null).toString());
  }

  @Test
  public void testParameters() {
    Assertions.assertEquals(
        "{content={application/json={schema={type=object, properties={ID={type=string, example=joe}}}}}}",
        OpenAPI.parameters(of("ID", of("type", "string", "sample", "joe"))).toString());
  }

  @Test
  public void testContent() throws Exception {
    Assertions.assertEquals("{content={application/json={schema={type=object}}}}",
        OpenAPI.content().toString());
  }

  @Test
  public void testProperty() throws Exception {
    Property p = new Property();
    p.name = "col";
    p.dbType = "VARCHAR";
    p.type = "string";
    Assertions.assertEquals("{type=string, x-dbType=VARCHAR}", OpenAPI.property(p).toString());
    p.pkpos = 0;
    Assertions.assertEquals("{type=string, x-dbType=VARCHAR, x-pkPos=0}",
        OpenAPI.property(p).toString());
    p.ref = "dj/db/table/col";
    p.pkpos = null;
    Assertions.assertEquals("{type=string, x-dbType=VARCHAR, x-ref=dj/db/table/col}",
        OpenAPI.property(p).toString());
    p.ref = null;
    p.readOnly = true;
    Assertions.assertEquals("{type=string, x-dbType=VARCHAR, readOnly=true}",
        OpenAPI.property(p).toString());
  }

  @Test
  public void testTable() throws Exception {
    Property id = new Property();
    id.pkpos = 0;
    id.name = "ID";
    id.dbType = "INT";
    id.type = "number";
    Property name = new Property();
    name.name = "name";
    name.dbType = "VARCHAR";
    name.type = "string";
    Table table = Table.ofName("table");
    table.properties = MapUtil.of("ID", id, "name", name);
    Assertions.assertEquals(
        "{table={type=object, properties={ID={type=number, x-dbType=INT, x-pkPos=0}, name={type=string, x-dbType=VARCHAR}}, required=[ID]}}",
        OpenAPI.table(table).toString());
  }

  @Test
  public void testMatchPath() {
    Assertions.assertEquals(null, OpenAPI.matchPath("/a", "/b"));
    Assertions.assertEquals(of(), OpenAPI.matchPath("/a", "/a"));
    Assertions.assertEquals(of("a", "b"), OpenAPI.matchPath("/{a}", "/b"));
    Assertions.assertEquals(of("a", "b"), OpenAPI.matchPath("/{a}/ccc", "/b/ccc"));
    Assertions.assertEquals(null, OpenAPI.matchPath("/{a}/ccc", "/b/ddd"));
    Assertions.assertEquals(of("a", "b"), OpenAPI.matchPath("/--{a}--/ccc", "/--b--/ccc"));
    Assertions.assertEquals(null, OpenAPI.matchPath("/--{a}--/ccc", "/b/ccc"));
    Assertions.assertEquals(of("a", "b", "c", "d"), OpenAPI.matchPath("/{a}/{c}", "/b/d"));
  }
}
