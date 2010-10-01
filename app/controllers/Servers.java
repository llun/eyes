package controllers;

import java.util.Date;
import java.util.Set;

import models.Invite;
import models.Quota;
import models.Server;
import models.Server.Status;
import models.ServerEventLog;
import models.User;
import models.probe.Probe;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Servers extends Controller {

  public static void index() {
    Set<Server> servers = Server.forUser(Security.connected());
    User user = User.fromUsername(Security.connected());
    Quota quota = Quota.forUsername(user.username);
    render(user, servers, quota);
  }

  public static void show(@Required Long server) {
    if (!Validation.hasError("server")) {
      Server instance = Server.findById(server);
      if (instance != null) {
        Quota quota = Quota.forUsername(Security.connected());
        Set<String> types = quota.allowProbes();
        Probe[] probes = instance.probes();
        User[] responders = instance.activeResponders();
        Invite[] invites = Invite.forServer(instance);

        render(instance, types, probes, responders, invites, quota);
      }
    }
    index();
  }

  public static void create(@Required String name) {
    if (Validation.hasErrors()) {
      Validation.keep();
      params.flash();
    } else {
      Server server = Server.create(Security.connected(), name);
      if (server == null) {
        Validation.addError("name", Messages.get("server.duplicated"));
        Validation.keep();
      }
    }
    index();
  }

  public static void delete(@Required Long server) {
    if (!Validation.hasErrors()) {
      Server instance = Server.findById(server);
      User user = User.fromUsername(Security.connected());
      if (instance != null && instance.owner.equals(user)) {
        instance.destroy();
      }
    }
    index();
  }

  public static void eventCount(@Required Long server, Status status,
      Date begin, Date end) {
    Server instance = Server.findById(server);
    if (instance != null) {
      renderText(ServerEventLog.eventCount(instance, status, begin, end));
    }
  }
}
