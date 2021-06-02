package org.dashjoin.service;

import org.dashjoin.service.Manage.Version;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * tests the main rest entry point
 */
public class RESTTest {

  @Test
  public void testManage() {
    new Manage().version();
    for (Version d : new Manage().getDrivers())
      if (d.name.equals("org.h2.Driver"))
        return;
    Assert.fail();
  }

  @Test
  public void testManage2() {
    for (Version d : new Manage().getDatabases())
      if (d.name.equals("org.dashjoin.service.SQLDatabase"))
        return;
    Assert.fail();
  }

  @Test
  public void testManage3() {
    for (Version d : new Manage().getFunctions())
      if (d.name.equals("org.dashjoin.function.AlterTableTrigger"))
        return;
    Assert.fail();
  }
}
