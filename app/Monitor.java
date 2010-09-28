import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import models.Server;
import models.Server.Status;
import models.User;
import models.probe.Probe;
import play.Logger;
import play.i18n.Messages;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;

@Every("5mn")
public class Monitor extends Job {

  public void doJob() {

    Logger.info("Checking probes");
    Probe[] probes = Server.allProbes();
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
      StringBuffer buffer = new StringBuffer();
      Probe serverProbes[] = server.probes();
      for (Probe probe : serverProbes) {
        if (!probe.status() && server.status == Status.UP) {
          server.status = Status.DOWN;
        }

        buffer.append(String.format("%s: %s\n<br />\n", probe.name(),
            probe.status() ? "OK" : "This probe have something wrong"));
      }

      if (server.status == Status.DOWN) {
        server.message = Messages.get("server.probe.down");
      } else {
        server.message = "";
      }
      server.save();

      if (server.status == Status.DOWN && server.probes().length > 0
          && server.responders.size() > 0) {
        ArrayList<String> recipients = new ArrayList<String>();
        Set<User> responders = server.responders;
        for (User responder : responders) {
          recipients.add(responder.email);
        }
        Mail.send("alert@throughwave.com", "alert@throughwave.com",
            recipients.toArray(new String[recipients.size()]),
            String.format("%s have some problems", server.name),
            buffer.toString(),
            "Something wrong please login to monitor.nytes.net", "text/html",
            new File[0]);
      }

    }

  }
}
