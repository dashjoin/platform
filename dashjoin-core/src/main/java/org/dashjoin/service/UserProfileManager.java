package org.dashjoin.service;

import java.util.List;

/**
 * user management
 */
public interface UserProfileManager {

  /**
   * user info
   */
  public static class UserInfo {
    public String ID;
    public Long created;
    public Long lastLogin;
    public String stripeId;
    public String subscription;
    public List<String> groups;
  }

  /**
   * lookup / create user info
   */
  UserInfo getOrCreate(String userid) throws Exception;

  /**
   * update user info. returns true is change was made
   */
  public boolean update(String userid, UserInfo user) throws Exception;
}
