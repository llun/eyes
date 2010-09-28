package controllers;

import java.util.List;

import models.User;
import models.User.Role;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Users extends Controller {

  public static void profile() {
    User user = User.fromUsername(Security.connected());
    render(user);
  }

  public static void changePassword(@Required Long user,
      @Required String password, @Required String confirm) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
    } else {
      if (!password.equals(confirm)) {
        Validation.addError("confirm", Messages.get("user.register.confirmnotmatch"));
        Validation.keep();
      } else {
        User instance = User.findById(user);
        instance.changePassword(password);
      }
    }
    profile();
  }

  @Check("admin")
  public static void list() {
    List<User> users = User.find("order by username").fetch();
    render(users);
  }

  @Check("admin")
  public static void create(@Required String username,
      @Required String password, @Required String email, @Required String role) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
    } else {
      boolean result = User.register(username, password, email);
      if (result) {
        User user = User.find("byUsername", username).first();
        user.role = Role.valueOf(role);
        user.save();
      }
    }
    list();
  }

  @Check("admin")
  public static void destroy(@Required Long user) {
    User instance = User.findById(user);
    if (instance != null) {
      instance.destroy();
    }
    list();
  }

}
