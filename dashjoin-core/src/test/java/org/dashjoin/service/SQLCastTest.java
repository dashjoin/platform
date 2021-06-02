package org.dashjoin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import org.dashjoin.model.AbstractDatabase;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SQLCastTest extends JsonCastTest {

  @Override
  AbstractDatabase db() {
    return new SQLDatabase();
  }

  @Test
  public void testDate() throws Exception {
    Assert.assertTrue(db().cast(p("date", "time"), "10:33:26") instanceof LocalTime);
    Assert.assertTrue(db().cast(p("date", "DATE"), "2020-02-12") instanceof Date);
    Assert.assertTrue(db().cast(p("date", "DATE"), "2021-01-19T10:33:26+00:00") instanceof Date);
    Assert.assertTrue(db().cast(p("date", "DATE"), "2021-01-19T10:33:26Z") instanceof Date);
    Assert.assertTrue(db().cast(p("date", "DATE"), "2021-11-06 05:20:44") instanceof Date);
    Assert.assertTrue(db().cast(p("date", "DATE"), "2021-11-06 05:20") instanceof Date);
    // System.out.println(db().cast(p("date", "DATE"), "2021-11-06 05:20"));
  }

  @Test
  public void testDatePostgres() throws Exception {
    SQLDatabase db = (SQLDatabase) db();
    db.url = "jdbc:postgresql:blabla";
    Assert.assertTrue(db.cast(p("date", "DATE"), "2021-01-19T10:33:26Z") instanceof LocalDate);
    Assert.assertTrue(db.cast(p("date", "time"), "10:33:26") instanceof LocalTime);
    Assert.assertTrue(
        db.cast(p("date", "timestamp"), "2021-01-19T10:33:26Z") instanceof LocalDateTime);
    Assert.assertTrue(
        db.cast(p("date", "timestampz"), "2021-01-19T10:33:26Z") instanceof OffsetDateTime);
  }
}
