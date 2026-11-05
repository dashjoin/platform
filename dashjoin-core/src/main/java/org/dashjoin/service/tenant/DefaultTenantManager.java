package org.dashjoin.service.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import org.dashjoin.util.DJRuntime;
import org.dashjoin.util.RuntimeDefinitions;

/**
 * default tenant (is called DJ)
 */
@ApplicationScoped
@DJRuntime(RuntimeDefinitions.ONPREMISE)
@Default
public class DefaultTenantManager implements TenantManager {
  private String tenantId = "dj";

  @Override
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
