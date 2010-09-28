package controllers;

import models.Quota;
import models.Server;
import models.User;
import models.probe.HTTPProbe;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class HTTPProbes extends Controller {

  public static void create(@Required Long server, @Required String name,
      @Required String serverURL, @Required Integer expectResponse) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      flash.put("message", Messages.get("probe.http.error"));
      flash.keep();
    } else {
      Server instance = Server.findById(server);
      if (Quota.canCreateProbe(User.fromUsername(Security.connected()),
          instance)) {
        HTTPProbe probe = new HTTPProbe(instance, name, serverURL,
            expectResponse);
        probe.save();
      }
    }

    Servers.show(server);
  }

  public static void check(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      HTTPProbe instance = HTTPProbe.findById(probe);
      instance.status = instance.check();
      instance.save();
    }
    Servers.show(server);
  }

  public static void delete(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      HTTPProbe instance = HTTPProbe.findById(probe);
      if (instance != null) {
        instance.delete();
      }
    }
    Servers.show(server);
  }

}
