package org.dashjoin.service;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.dashjoin.function.FunctionService;
import org.dashjoin.model.Table;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import io.quarkus.scheduler.Trigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.java.Log;

@Log
@Startup
@ApplicationScoped
public class Cron {

  @Inject
  Scheduler scheduler;

  @Inject
  Services services;

  @Inject
  FunctionService function;

  /**
   * startup bean must not throw exceptions
   */
  public void onStart(@Observes StartupEvent ev) {
    try {
      resetSchedule();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error while starting scheduler", e);
    }
  }

  public Map<String, Object> resetSchedule() throws Exception {

    // map of job to error / next runtime
    Map<String, Object> res = new HashMap<>();

    // unschedule all jobs
    for (Trigger t : scheduler.getScheduledJobs())
      scheduler.unscheduleJob(t.getId());

    // jobs run as "scheduler"
    SecurityContext sc = new SecurityContext() {
      @Override
      public boolean isUserInRole(String role) {
        return role.equals("scheduler");
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getAuthenticationScheme() {
        return null;
      }
    };

    // schedule any function with "cron" or "every"
    for (Map<String, Object> f : services.getConfig().getConfigDatabase()
        .all(Table.ofName("dj-function"), null, null, null, false, null)) {
      String id = (String) f.get("ID");
      String cron = (String) f.get("cron");
      String every = (String) f.get("every");
      if (cron != null) {
        try {
          log.info("scheduling " + id + " " + cron);
          Trigger t = scheduler.newJob((String) id).setCron(cron).setTask(ctx -> {
            try {
              log.info("running cron scheduled " + id);
              function.call(sc, ctx.getTrigger().getId(), null);
              log.info(ctx.getTrigger().getId() + " complete");
            } catch (Exception e) {
              log.log(Level.SEVERE, "error running scheduled job " + id, e);
            }
          }).setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP).schedule();
          res.put(t.getId(), "" + t.getNextFireTime());
        } catch (Exception cronEx) {
          log.warning("cron syntax error: " + cronEx);
          res.put(id, cronEx.toString());
        }
      }
      if (every != null) {
        try {
          log.info("scheduling " + id + " " + every);
          Trigger t = scheduler.newJob((String) id).setInterval(every).setTask(ctx -> {
            try {
              log.info("running interval scheduled " + id);
              function.call(sc, ctx.getTrigger().getId(), null);
              log.info(ctx.getTrigger().getId() + " complete");
            } catch (Exception e) {
              log.log(Level.SEVERE, "error running scheduled job " + id, e);
            }
          }).setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP).schedule();
          res.put(t.getId(), "" + t.getNextFireTime());
        } catch (Exception cronEx) {
          log.warning("cron syntax error: " + cronEx);
          res.put(id, cronEx.toString());
        }
      }
    }
    return res;
  }
}
