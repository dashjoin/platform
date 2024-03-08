package org.dashjoin.service.tenant;

/**
 * defines the DJ tenant instance ID
 */
public interface TenantManager {

  void setTenantId(String tenant);

  String getTenantId();

  default String getHomePrefix() {
    return "";
  }
}
