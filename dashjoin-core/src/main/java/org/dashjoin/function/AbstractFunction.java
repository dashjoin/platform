package org.dashjoin.function;

import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.service.Services;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

/**
 * base class for all functions.
 */
public abstract class AbstractFunction<ARG, RET> implements Function<ARG, RET> {

  @JsonIgnore
  protected SecurityContext sc;

  @JsonIgnore
  protected Services services;

  @JsonIgnore
  protected ExpressionService expressionService;

  @JsonIgnore
  protected boolean readOnly;

  public void init(SecurityContext sc, Services services, ExpressionService expressionService,
      boolean readOnly) {
    Preconditions.checkNotNull(sc);
    Preconditions.checkNotNull(services);
    Preconditions.checkNotNull(expressionService);
    this.sc = sc;
    this.services = services;
    this.expressionService = expressionService;
    this.readOnly = readOnly;
  }
}
