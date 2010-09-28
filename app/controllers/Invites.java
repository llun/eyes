package controllers;

import java.util.HashMap;

import models.Invite;
import models.Invite.Result;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Router;

public class Invites extends Controller {

  @Check("user")
  public static void invite(@Required Long server,
      @Required @Email String email, @Required String code) throws Throwable {

    if (Validation.hasError("server") || Validation.hasError("email")
        || Validation.hasError("code")) {
      Application.index();
    } else {
      Result result = Invite.acceptResponder(server, email, code);
      if (result == Result.OK) {
        if (Security.isConnected()) {
          Application.index();
        } else {
          Secure.login();
        }
      } else if (result == Result.NOUSER) {
        render(server, email, code);
      } else {
        Application.index();
      }
    }

  }

  @Check("user")
  public static void delete(@Required Long server, @Required Long invite) {
    if (!Validation.hasErrors()) {
      Invite instance = Invite.findById(invite);
      instance.delete();
    }

    HashMap<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("server", server);
    String URL = Router.getFullUrl("Servers.show", arguments).concat("#responder");
    redirect(URL);
  }

  public static void register(@Required Long server, @Required String username,
      @Required String password, @Required String confirm,
      @Required String email, @Required String code) throws Throwable {
    if (Validation.hasErrors()) {
      Validation.keep();
      params.flash();
      invite(server, email, code);
    } else {
      if (password.equals(confirm)) {
        if (Invite.registerResponder(server, username, password, code)) {
          Secure.login();
        } else {
          Validation.addError("username", "user.register.duplicate");
          Validation.keep();
          invite(server, email, code);
        }
      } else {
        Validation.addError("confirm", "user.register.confirmnotmatch");
        Validation.keep();
        params.flash();
        invite(server, email, code);
      }
    }
  }

}
