package controllers;

import models.Quota;
import models.Server;
import models.User;
import models.probe.SSHLoginProbe;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class SSHLoginProbes extends Controller {

  public static void create(@Required Long server, @Required String name,
      @Required String address, @Required String username,
      @Required String password) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      flash.put("message", Messages.get("probe.sshlogin.error"));
    } else {
      Server instance = Server.findById(server);
      if (Quota.canCreateProbe(User.fromUsername(Security.connected()),
          instance)) {
        SSHLoginProbe probe = new SSHLoginProbe(instance, name, username,
            password, address);
        probe.save();
      }
    }
    Servers.show(server);
  }

  public static void check(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      SSHLoginProbe instance = SSHLoginProbe.findById(probe);
      instance.status = instance.check();
      instance.save();
    }
    Servers.show(server);
  }

  public static void delete(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      SSHLoginProbe instance = SSHLoginProbe.findById(probe);
      if (instance != null) {
        instance.delete();
      }
    }
    Servers.show(server);
  }
}
