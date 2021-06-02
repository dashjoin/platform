package org.dashjoin.service;

import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;

/**
 * central handling of passwords in the config DB
 */
public class PasswordDatabase extends JSONDatabase {

  JSONDatabase db;

  public PasswordDatabase(JSONDatabase db) {
    this.db = db;
  }

  /**
   * Encrypt passwords with the system's credential manager. Ignore password ********
   * 
   * @param object Object that might contain a password as property (key "password")
   */
  void encryptPasswords(Map<String, Object> object) {
    // Password handling: encrypt any plaintext passwords prior to saving to disk
    Object p = object.get("password");
    if ("********".equals(p))
      object.remove("password");
    else if (p != null && p instanceof String)
      object.put("password", CredentialManager.encryptCredential((String) p));
  }

  /**
   * when reading, replace passwords with ********
   */
  void removePasswords(Map<String, Object> object) {
    if (object == null)
      return;
    Object p = object.get("password");
    if (p != null && p instanceof String)
      object.put("password", "********");
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    encryptPasswords(object);
    db.create(m, object);
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    Map<String, Object> res = db.read(s, search);
    removePasswords(res);
    return res;
  }

  @Override
  public boolean update(Table s, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    encryptPasswords(object);
    return db.update(s, search, object);
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    return db.delete(s, search);
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = db.queryMap(info, arguments);
    for (Map<String, Object> i : res.values())
      removePasswords(i);
    return res;
  }

  public String password(String table, String id) throws Exception {
    Map<String, Object> res = db.read(Table.ofName(table), MapUtil.of("ID", id));
    Object password = res != null ? res.get("password") : null;
    if (password instanceof String)
      return (String) password;
    return null;
  }
}
