package org.dashjoin.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation that allows tagging the fields of instances of AbstractDatabase
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSchema {

  /**
   * json schema enums
   */
  public String[] enums() default {};

  /**
   * json schema required
   */
  public String[] required() default {};

  /**
   * json schema form widget
   */
  public String widget() default "";

  /**
   * json schema form widget custom widgetType
   */
  public String widgetType() default "";

  /**
   * json schema title
   */
  public String title() default "";

  /**
   * json schema description
   */
  public String description() default "";

  /**
   * HTTP REST URL to call in order to get the choices
   */
  public String choicesUrl() default "";

  /**
   * HTTP verb to use for the call
   */
  public String choicesVerb() default "";

  /**
   * jsonata to apply in case the result is not a list of strings
   */
  public String jsonata() default "";

  /**
   * property layout info
   */
  public String layout() default "";

  /**
   * static choices
   */
  public String[] choices() default {};

  public boolean readOnly() default false;

  public boolean createOnly() default false;

  public String[] style() default {};

  public String[] order() default {};

  public String displayWith() default "";

  public String computed() default "";
}
