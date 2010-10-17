import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import models.ProbeEventLog;
import models.Server;
import models.Server.Status;
import models.ServerEventLog;
import models.probe.Probe;
import models.probe.ProbeResult;
import notifiers.Mails;
import play.Logger;
import play.i18n.Messages;
import play.jobs.Every;
import play.jobs.Job;

@Every("5mn")
public class Monitor extends Job {

  public void doJob() {

    Logger.info("Checking probes");
    Collection<Probe> probes = Server.activeProbes();
    for (Probe probe : probes) {
      try {
        Class probeClass = probe.getClass();

        Field field = probeClass.getField("status");
        field.set(probe, probe.check());

        Method method = probeClass.getMethod("save");
        method.invoke(probe);
      } catch (Exception e) {
        Logger.error(e, "Can't save probe status");
      }
    }

    List<Server> servers = Server.all().fetch();
    for (Server server : servers) {
      server.status = Status.UP;
      Probe serverProbes[] = server.probes();
      Integer totalDown = 0;
      for (Probe probe : serverProbes) {
        ProbeResult status = probe.status();
        if (!status.success) {
          totalDown++;

          if (server.status == Status.UP) {
            server.status = Status.SOME_DOWN;
          }
        }

        ProbeEventLog.submit(probe.getId(), probe.type(), status.success,
            status.message);
      }

      if (totalDown == serverProbes.length) {
        server.status = Status.DOWN;
      }

      if (server.status == Status.DOWN || server.status == Status.SOME_DOWN) {
        server.message = Messages.get("server.probe.down");
      } else {
        server.message = "";
      }
      server.save();
      ServerEventLog.submit(server, server.status, server.message);

      boolean allFail = server.alertWhenAllFail == null ? false
          : server.alertWhenAllFail;
      if (allFail) {
        if (server.status == Status.DOWN) {
          Mails.alert(server);
        }
      } else {
        if (server.status == Status.DOWN || server.status == Status.SOME_DOWN) {
          Mails.alert(server);
        }
      }

    }

  }
}
