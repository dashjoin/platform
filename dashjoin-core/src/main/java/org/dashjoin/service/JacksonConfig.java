package org.dashjoin.service;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * make sure jackson serializes Dates as ISO 8601 strings instead of timestamps (long). see
 * https://weblog.west-wind.com/posts/2014/jan/06/javascript-json-date-parsing-and-real-dates#json-dates-are-not-dates--they-are-strings
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

  /**
   * singleton mapper
   */
  private final ObjectMapper objectMapper;

  /**
   * do not write dates as timestamps, use ISO date encoding instead
   */
  public JacksonConfig() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  /**
   * get object mapper context
   */
  @Override
  public ObjectMapper getContext(Class<?> c) {
    return objectMapper;
  }
}
