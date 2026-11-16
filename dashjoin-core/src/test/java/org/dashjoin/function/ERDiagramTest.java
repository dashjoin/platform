package org.dashjoin.function;

import java.util.Arrays;
import java.util.LinkedHashMap;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.SQLDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ERDiagramTest {

  @Test
  public void testNote() {
    ERDiagram e = new ERDiagram();
    Assertions.assertEquals("'title'", e.note("title", null));
    Assertions.assertEquals("'comment'", e.note(null, "comment"));
    Assertions.assertEquals("'title comment'", e.note("title", "comment"));
    Assertions.assertEquals("'it\\'s'", e.note(null, "it's"));
  }

  @Test
  public void testWord() {
    Assertions.assertEquals("ID", ERDiagram.word("ID", "_"));
    Assertions.assertEquals("order_date", ERDiagram.word("order date", "_"));
    Assertions.assertEquals("_1st", ERDiagram.word("1st", "_"));
    Assertions.assertEquals("_ber", ERDiagram.word("über", "_"));
    Assertions.assertEquals("DECIMAL(10_2)", ERDiagram.word("DECIMAL(10,2)", "_"));
    Assertions.assertEquals("unknown", ERDiagram.word(null, "unknown"));
  }

  @Test
  public void testQuote() {
    Assertions.assertEquals("\"it's 'x' y\"", ERDiagram.quote("it's \"x\"\ny"));
  }

  static Property prop(String name, String type) {
    Property p = new Property();
    p.name = name;
    p.dbType = type;
    return p;
  }

  static AbstractDatabase db() {
    return db("northwind");
  }

  static AbstractDatabase db(String dbName) {
    SQLDatabase db = new SQLDatabase();
    db.name = dbName;

    Table customers = Table.ofName("CUSTOMERS");
    customers.properties = new LinkedHashMap<>();
    Property id = prop("CUSTOMER_ID", "VARCHAR");
    id.pkpos = 0;
    customers.properties.put(id.name, id);
    Property name = prop("company name", "VARCHAR");
    name.comment = "the \"name\"";
    customers.properties.put(name.name, name);

    Table orders = Table.ofName("ORDERS");
    orders.properties = new LinkedHashMap<>();
    Property oid = prop("ORDER_ID", "INTEGER");
    oid.pkpos = 0;
    orders.properties.put(oid.name, oid);
    Property cust = prop("CUSTOMER_ID", "VARCHAR");
    cust.ref = "dj/" + dbName + "/CUSTOMERS/CUSTOMER_ID";
    cust.required = true;
    orders.properties.put(cust.name, cust);
    Property tags = prop("TAGS", null);
    tags.type = "array";
    tags.items = new Property();
    tags.items.ref = "dj/" + dbName + "/CUSTOMERS/CUSTOMER_ID";
    orders.properties.put(tags.name, tags);

    Table details = Table.ofName("order details");
    details.properties = new LinkedHashMap<>();
    Property a = prop("ORDER_ID", "INTEGER");
    a.pkpos = 0;
    a.ref = "dj/" + dbName + "/ORDERS/ORDER_ID";
    details.properties.put(a.name, a);
    Property b = prop("PRODUCT_ID", "INTEGER");
    b.pkpos = 1;
    details.properties.put(b.name, b);

    db.tables = new LinkedHashMap<>();
    db.tables.put(customers.name, customers);
    db.tables.put(orders.name, orders);
    db.tables.put(details.name, details);
    return db;
  }

  @Test
  public void testMermaid() {
    String s = new ERDiagram().mermaid(Arrays.asList(db()));
    Assertions.assertEquals("erDiagram\n" //
        + "  CUSTOMERS {\n" //
        + "    VARCHAR CUSTOMER_ID PK\n" //
        + "    VARCHAR company_name \"company name: the 'name'\"\n" //
        + "  }\n" //
        + "  ORDERS {\n" //
        + "    INTEGER ORDER_ID PK\n" //
        + "    VARCHAR CUSTOMER_ID FK\n" //
        + "    array TAGS FK\n" //
        + "  }\n" //
        + "  order_details[\"order details\"] {\n" //
        + "    INTEGER ORDER_ID PK, FK\n" //
        + "    INTEGER PRODUCT_ID PK\n" //
        + "  }\n" //
        + "  ORDERS }o--|| CUSTOMERS : \"CUSTOMER_ID\"\n" //
        + "  ORDERS }o--o{ CUSTOMERS : \"TAGS\"\n" //
        + "  order_details }o--o| ORDERS : \"ORDER_ID\"\n", s);
  }

  @Test
  public void testMermaidDuplicateNames() {
    String s = new ERDiagram().mermaid(Arrays.asList(db(), db("other")));
    Assertions.assertTrue(s.contains("  CUSTOMERS_2[\"CUSTOMERS\"] {\n"));
    Assertions.assertTrue(s.contains("  order_details_2[\"order details\"] {\n"));
    // relationships resolve within their own database
    Assertions.assertTrue(s.contains("  ORDERS_2 }o--|| CUSTOMERS_2 : \"CUSTOMER_ID\"\n"));
  }

  @Test
  public void testDbml() {
    String s = new ERDiagram().dbml(Arrays.asList(db()));
    Assertions.assertTrue(s.contains("  CUSTOMER_ID VARCHAR [ref: > CUSTOMERS.CUSTOMER_ID]\n"));
    Assertions.assertTrue(s.contains("  indexes {(ORDER_ID, PRODUCT_ID) [pk]}\n"));
  }
}
