package controllers;

import models.User;
import models.User.Role;

public class Security extends Secure.Security {

  public static boolean authenticate(String username, String password) {
    return User.authenticate(username, password);
  }

  public static boolean check(String profile) {
    Role role = Role.valueOf(profile.toUpperCase());
    User user = User.find("byUsername", connected()).first();
    return user.role.ordinal() <= role.ordinal();
  }

}
