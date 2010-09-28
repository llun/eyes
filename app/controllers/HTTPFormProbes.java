package controllers;

import java.util.HashMap;

import models.Quota;
import models.Server;
import models.User;
import models.probe.HTTPFormProbe;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class HTTPFormProbes extends Controller {

  public static void create(@Required Long server, @Required String name,
      @Required String serverURL, @Required Integer expectResponse,
      String[] keys, String[] values) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      flash.put("message", Messages.get("probe.http.form.error"));
    } else {
      Server instance = Server.findById(server);
      if (Quota.canCreateProbe(User.fromUsername(Security.connected()),
          instance)) {
        HashMap<String, String> properties = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
          if (keys[i].trim().length() > 0) {
            properties.put(keys[i], values[i]);
          }
        }

        HTTPFormProbe probe = new HTTPFormProbe(instance, name, serverURL,
            expectResponse, properties);
        probe.save();
      }
    }
    Servers.show(server);
  }

  public static void check(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      HTTPFormProbe instance = HTTPFormProbe.findById(probe);
      instance.status = instance.check();
      instance.save();
    }
    Servers.show(server);
  }

  public static void delete(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      HTTPFormProbe instance = HTTPFormProbe.findById(probe);
      if (instance != null) {
        instance.delete();
      }
    }
    Servers.show(server);

  }

}
