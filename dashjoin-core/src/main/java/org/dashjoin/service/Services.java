package org.dashjoin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dashjoin.expression.ExpressionPreviewService;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.function.FunctionService;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.tenant.DefaultTenantManager;
import org.dashjoin.service.tenant.TenantManager;
import org.dashjoin.util.Home;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;

/**
 * central service registry. Provides in process getters as well as JAX-RS singletons for services
 * and exception handlers
 */
@ApplicationScoped
@Log
public class Services {

  public static final String REST_PREFIX = "rest/";

  @Inject
  Services(TenantManager tenantManager) {
    this.tenantManager = tenantManager;

    // Only log env and settings on production startup
    if (log.isLoggable(Level.CONFIG) && System.getenv("GAE_VERSION") != null)
      logConfigSettings();
  }

  public void logConfigSettings() {
    log.info("--- Dashjoin Services Startup Sequence ---");
    log.info(">> System properties:");
    new TreeMap<>(System.getProperties()).forEach((k, v) -> {
      log.info(k + "=" + v);
    });

    log.info(">> Environment:");
    new TreeMap<>(System.getenv()).forEach((k, v) -> {
      log.info(k + "=" + v);
    });
  }

  /**
   * Tenant cache
   * 
   * Evicts tenants after timeout, important for long living multi tenant hosting (i.e. PaaS,
   * Playground)
   */
  final static String TENANT_CACHE_SPEC = "expireAfterAccess=30m";

  final static Cache<String, Config> tenantCache =
      Caffeine.from(TENANT_CACHE_SPEC).removalListener((k, v, c) -> {
        PojoDatabase pojo = (PojoDatabase) v;
        Map<String, AbstractDatabase> cache = pojo.cache();
        if (cache != null) {
          List<String> dbs = new ArrayList<>(cache.keySet());
          log.info("evicting pojo db - closing connection pools for " + dbs);
          for (String db : dbs)
            pojo.removeCache(db);
        }
      }).build();

  final static String NULL_KEY = "<<<NULL>>>";

  // @Inject
  public TenantManager tenantManager;

  public TenantManager getTenantManager() {
    return tenantManager;
  }

  /**
   * Initializes the config DB. It is setup as union(classpath, file system) with the file system
   * being read/write and having preference over the other DBs. Other providers may be added later
   * on (e.g. when a DB is defined)
   */
  synchronized public Config getConfig() {
    String id = tenantManager.getTenantId();
    String key = id != null ? id : NULL_KEY;

    Config c = tenantCache.getIfPresent(key);
    if (c == null) {
      c = pojoDatabase();
      // Put the config into the cache
      // Important: metadataCollection() might call getConfig() recursively!
      tenantCache.put(key, c);

      // make sure all DBs are initialized
      log.info("Starting metadata collection for tenant=" + id);
      c.metadataCollection();
      log.info("Done metadata collection for tenant=" + id);
    }
    return c;
  }

  @Inject
  JSONDatabase persistantDB;

  @Inject
  @JSONReadonlyDatabase
  JSONDatabase readonlyDB;

  @Inject
  PojoDatabase pojoDatabase() {
    PojoDatabase config = new PojoDatabase(this);
    config._cache = new ConcurrentHashMap<>();
    config.name = "config";
    config.ID = "dj/config";
    config._user = persistantDB;
    config._dbs = new CopyOnWriteArrayList<JSONDatabase>();
    JSONDatabase cl = readonlyDB;
    config._dbs.add(cl);
    config._dbs.add(new PolymorphismDatabase());
    return config;
  }

  /**
   * get the ID of this DJ instance
   */
  public String getDashjoinID() {
    // TODO: hard coded string
    return "dj";
  }

  /**
   * Home folder with tenant prefix appended in playground mode
   */
  public String getTenantHome() {
    return Home.get().getHome() + tenantManager.getHomePrefix();
  }

  /**
   * legacy Jetty non DI mode
   */
  public Services() {}

  /**
   * define REST services, exception mapper and jackson config.
   * 
   * register swagger at http://localhost:8080/rest/openapi.json. The swagger UI is not included by
   * default. IT needs to be downloaded from https://swagger.io/tools/swagger-ui/ and extracted into
   * any location in the webapps folder (due to CORS constraints, the API metadata cannot be
   * accessed from UIs served from different hosts).
   */
  // @Override
  public Set<Object> getSingletons() {
    if (persistantDB == null) {
      try {
        tenantManager = new DefaultTenantManager();
        persistantDB = new JSONFileDatabase();
        readonlyDB = new JSONClassloaderDatabase();
        Data data = new Data();
        data.services = this;
        QueryEditor.Delegate delegate = new QueryEditor.Delegate();
        delegate.services = this;
        Manage manage = new Manage();
        manage.services = this;
        manage.data = data;
        FunctionService function = new FunctionService();
        // function.services = this;
        FieldUtils.writeField(function, "services", this, true);
        // function.data = data;
        FieldUtils.writeField(function, "data", data, true);
        ExpressionService expression = new ExpressionService();
        // expression.services = this;
        FieldUtils.writeField(expression, "services", this, true);
        // expression.data = data;
        FieldUtils.writeField(expression, "data", data, true);
        // expression.function = function;
        FieldUtils.writeField(expression, "function", function, true);
        // MappingService mapping = new MappingService();
        // mapping.services = this;
        // FieldUtils.writeField(mapping, "services", this, true);
        // mapping.expressionService = expression;
        // FieldUtils.writeField(mapping, "expressionService", expression, true);
        data.expression = expression;
        // expression.manage = manage;
        FieldUtils.writeField(expression, "manage", manage, true);
        ExpressionPreviewService ep = new ExpressionPreviewService();
        // ep.expression = expression;
        FieldUtils.writeField(ep, "expression", expression, true);
        return Sets.newHashSet(data, delegate, manage, function, expression, /* mapping, */ ep,
            new ExMapper(), new JacksonConfig(),
            new ACLContainerRequestFilter() /*
                                             * , new BasicAuth() , new OpenApiResource()
                                             */);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else
      return null; // super.getSingletons();
  }
}
