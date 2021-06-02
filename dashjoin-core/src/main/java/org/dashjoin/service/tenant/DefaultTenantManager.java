package org.dashjoin.service.tenant;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import org.dashjoin.util.DJRuntime;
import org.dashjoin.util.RuntimeDefinitions;
import lombok.Getter;
import lombok.Setter;

/**
 * default tenant (is called DJ)
 */
@ApplicationScoped
@DJRuntime(RuntimeDefinitions.ONPREMISE)
@Default
public class DefaultTenantManager implements TenantManager {
  @Getter
  @Setter
  private String tenantId = "dj";
}
