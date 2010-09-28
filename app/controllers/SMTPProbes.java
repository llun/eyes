package controllers;

import models.Quota;
import models.Server;
import models.User;
import models.probe.SMTPProbe;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class SMTPProbes extends Controller {

  public static void create(@Required Long server, @Required String name,
      @Required String recipient, @Required String sender,
      @Required String address, @Required String username,
      @Required String password) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      flash.put("message", Messages.get("probe.smtp.error"));
    } else {
      Server instance = Server.findById(server);
      if (Quota.canCreateProbe(User.fromUsername(Security.connected()),
          instance)) {
        SMTPProbe probe = new SMTPProbe(instance, name, recipient, sender,
            username, password, address);
        probe.save();
      }
    }
    Servers.show(server);
  }

  public static void check(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      SMTPProbe instance = SMTPProbe.findById(probe);
      instance.status = instance.check();
      instance.save();
    }
    Servers.show(server);
  }

  public static void delete(@Required Long server, @Required Long probe) {
    if (!Validation.hasErrors()) {
      SMTPProbe instance = SMTPProbe.findById(probe);
      if (instance != null) {
        instance.delete();
      }
    }
    Servers.show(server);
  }

}
