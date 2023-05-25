package org.dashjoin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.dashjoin.model.Table;
import org.dashjoin.util.DJRuntime;
import org.dashjoin.util.RuntimeDefinitions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

/**
 * user profile manager on top of the JSON config DB
 */
@ApplicationScoped
@DJRuntime(RuntimeDefinitions.ONPREMISE)
@Default
@Log
public class DefaultUserProfileManager implements UserProfileManager {

  @Inject
  JSONDatabase jsonDb;
  private Table userTable;
  private ObjectMapper objectMapper;

  DefaultUserProfileManager() {
    objectMapper = new ObjectMapper();
    userTable = new Table();
    userTable.name = "__user__";
  }

  @Override
  public UserInfo getOrCreate(String userid) {
    UserInfo u = read(userid);
    if (u == null) {
      log.info("Create new user " + userid);
      u = new UserInfo();
      // u.ID = userid;
      write(u);
    }
    return u;
  }

  @Override
  public boolean update(String userid, UserInfo user) {
    return write(user);
  }

  private UserInfo read(String userid) {
    Map<String, Object> r, o = new HashMap<>();
    o.put("ID", userid);
    try {
      r = jsonDb.read(userTable, o);
      if (r == null)
        return null;
      String s = objectMapper.writeValueAsString(r);
      return objectMapper.readValue(s, UserInfo.class);

    } catch (Exception e) {
      return null;
    }
  }

  private boolean write(UserInfo u) {
    Map<String, Object> o = new HashMap<>();

    String json;
    try {
      json = objectMapper.writeValueAsString(u);
      o = objectMapper.readValue(json, JSONDatabase.tr);
    } catch (JsonProcessingException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    try {
      jsonDb.update(userTable, o);
      return true;
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not write user - conflict", e);
      return false;
    }
  }
}
