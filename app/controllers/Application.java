package controllers;

import models.User;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;

public class Application extends Controller {

  public static void index() throws Throwable {
    if (Security.isConnected()) {
      Servers.index();
    } else {
      int limit = Integer
          .parseInt(Play.configuration.getProperty("eyes.limit"));
      if (User.count() < limit) {
        render();
      } else {
        Secure.login();
      }

    }
  }

  public static void verify(@Required Long user, @Required String code)
      throws Throwable {
    Logger.info("Verify %d", user);
    if (!Validation.hasErrors()) {
      User instance = User.findById(user);
      if (instance != null) {
        if (instance.verify(code)) {
          Secure.login();
        }
      }
    }

    Application.index();
  }

  public static void register(@Required String username,
      @Required String password, @Required String confirm,
      @Required @Email String email) throws Throwable {
    if (Validation.hasErrors()) {
      Validation.keep();
      params.flash();
    } else {

      if (!password.equals(confirm)) {
        Validation.addError("confirm",
            Messages.get("user.register.confirmnotmatch"));
        Validation.keep();
        params.flash();
      } else {
        User.register(username, password, email);
      }

    }
    index();
  }
}