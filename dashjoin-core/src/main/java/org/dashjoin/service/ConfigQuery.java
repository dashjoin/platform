package org.dashjoin.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation that allows methods to be marked as queries that can be referenced from the catalog
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConfigQuery {

  /**
   * the query ID (matches the query catalog query)
   */
  String query();

  /**
   * the base table we operate on (needed to compute the query result metadata)
   */
  String table();
}
