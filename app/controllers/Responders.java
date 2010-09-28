package controllers;

import java.util.HashMap;

import models.Invite;
import models.Quota;
import models.Server;
import models.User;
import models.User.Role;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;

@With(Secure.class)
public class Responders extends Controller {

  public static void invite(@Required @Email String email) {
    if (!Validation.hasErrors()) {
      User user = User.fromEmail(email);
      if (user != null) {

      }
    } else {
      notFound();
    }
  }

  public static void create(@Required Long server, @Email String email) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
    } else {
      Server instance = Server.findById(server);
      if (Quota.canInviteResponder(User.fromUsername(Security.connected()),
          instance)) {
        if (Invite.inviteResponder(instance, email)) {
          flash.put("success", Messages.get("responder.invite.success"));
          flash.keep();
        }
      } else {
        flash.put("message", Messages.get("responder.invite.overquota"));
        flash.keep();
      }

    }

    HashMap<String, Object> arguments = new HashMap<String, Object>(1);
    arguments.put("server", server);
    String URL = Router.getFullUrl("Servers.show", arguments);
    redirect(URL.concat("#responder"));
  }

  public static void delete(@Required Long server, @Required Long user) {
    if (!Validation.hasErrors()) {
      Server serverInstance = Server.findById(server);
      User userInstance = User.findById(user);

      serverInstance.responders.remove(userInstance);
      serverInstance.save();

      if (userInstance.role == Role.RESPONDER) {
        userInstance.delete();
      }

    }

    HashMap<String, Object> arguments = new HashMap<String, Object>(1);
    arguments.put("server", server);
    String URL = Router.getFullUrl("Servers.show", arguments);
    redirect(URL.concat("#responder"));
  }

}
