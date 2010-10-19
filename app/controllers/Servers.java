package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import play.mvc.Router;
import play.mvc.With;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

  public static void events(Long server, Integer sEcho, Integer iDisplayStart,
      Integer iDisplayLength) {
    Server instance = Server.findById(server);

    long totalRecord = 0;
    long totalDisplayRecord = 0;

    JsonArray result = new JsonArray();

    if (instance != null) {
      
      int begin = iDisplayStart / iDisplayLength + 1;
      int length = iDisplayLength;
      
      List<ServerEventLog> logs = ServerEventLog.find("server = ? order by id desc", instance)
          .fetch(begin, length);
      totalRecord = ServerEventLog.count("byServer", instance);
      totalDisplayRecord = totalRecord;

      SimpleDateFormat formatter = new SimpleDateFormat("H:mm dd/MM/yyyy");
      for (ServerEventLog log : logs) {

        JsonArray record = new JsonArray();

        String date = formatter.format(log.created);
        String status = log.status == Status.UP ? "OK" : "Down";

        record.add(new JsonPrimitive(date));
        record.add(new JsonPrimitive(status));
        record.add(new JsonPrimitive(log.message));

        result.add(record);
      }
    }

    JsonObject object = new JsonObject();
    object.add("sEcho", new JsonPrimitive(sEcho));
    object.add("iTotalRecords", new JsonPrimitive(totalRecord));
    object.add("iTotalDisplayRecords", new JsonPrimitive(totalDisplayRecord));
    object.add("aaData", result);
    renderJSON(object.toString());
  }

  public static void eventCount(@Required Long server, Status status,
      Date begin, Date end) {
    Server instance = Server.findById(server);
    if (instance != null) {
      renderText(ServerEventLog.eventCount(instance, status, begin, end));
    }
  }

  public static void saveSettings(@Required Long server,
      boolean alertWhenAllFail) {

    Server instance = Server.findById(server);
    if (instance != null) {
      instance.alertWhenAllFail = alertWhenAllFail;
      instance.save();
    }

    HashMap<String, Object> arguments = new HashMap<String, Object>(1);
    arguments.put("server", server);
    String URL = Router.getFullUrl("Servers.show", arguments);
    redirect(URL.concat("#settings"));

  }
}
