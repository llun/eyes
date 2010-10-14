package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;

import notifiers.Mails;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import play.Logger;
import play.Play;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.libs.Codec;
import play.libs.Mail;
import play.mvc.Router;

@Entity
public class User extends Model {

  public static enum Role {
    ADMIN, USER, RESPONDER;
  }

  @Column(unique = true, nullable = false)
  public String username;
  @Column(nullable = false)
  public String hash;
  @Column(unique = true, nullable = false)
  public String email;
  public Role role;
  public String verifyCode;
  public Boolean active;
  public Boolean notifyViaMSN;

  public User(String username, String password, String email) {
    this.username = username;
    this.email = email;
    this.hash = Codec.hexMD5(password);
    this.role = Role.USER;
    this.active = false;

    Date date = new Date();
    this.verifyCode = Codec.hexMD5(Long.toString(date.getTime()));
  }

  public void changePassword(String newPassword) {
    hash = Codec.hexMD5(newPassword);
    save();
  }

  public void destroy() {
    Set<Server> servers = Server.forUser(username);
    for (Server server : servers) {
      if (server.owner.equals(this)) {
        server.destroy();
      } else {
        server.responders.remove(this);
        server.save();
      }
    }

    Quota quota = Quota.find("byUser", this).first();
    quota.delete();

    delete();
  }

  public boolean verify(String code) {
    boolean success = false;
    if (code.equals(verifyCode)) {
      this.active = true;
      this.verifyCode = "";
      success = true;
      save();
    }

    return success;
  }

  public void reject(String code) {
    if (code.equals(verifyCode)) {
      destroy();
    }
  }

  public String toString() {
    return String.format("%s<%s>", username, email);
  }

  public int hashCode() {
    return username.hashCode();
  }

  public boolean equals(Object other) {
    boolean result = false;
    if (other instanceof User) {
      User otherUser = (User) other;
      result = otherUser.username.equals(username);
    }
    return result;
  }

  public static User fromUsername(String username) {
    return User.find("byUsername", username.toLowerCase()).first();
  }

  public static User fromEmail(String email) {
    return User.find("byEmail", email.toLowerCase()).first();
  }

  public static boolean authenticate(String username, String password) {
    boolean result = false;

    User user = User.find("byUsername", username).first();
    if (user != null) {
      String hash = Codec.hexMD5(password);
      result = hash.equals(user.hash) && user.active;
    }

    return result;
  }

  public static boolean register(String username, String password, String email) {
    boolean result = false;
    String lcUsername = username.toLowerCase();
    String lcEmail = email.toLowerCase();

    User user = User.find("username like ? or email like ?", lcUsername,
        lcEmail).first();

    if (user == null) {
      user = new User(lcUsername, password, lcEmail);
      user.save();

      Quota quota = new Quota(user);
      quota.serverLimit = 1;
      quota.probeLimit = 3;
      quota.responderLimit = 3;
      quota.allowMSN = false;
      quota.allowProbes = "HTTP,HTTPForm,IMAP";
      quota.save();

      HashMap<String, Object> arguments = new HashMap<String, Object>();
      arguments.put("user", user.id);
      arguments.put("code", user.verifyCode);
      String verifyURL = Router.getFullUrl("Application.verify", arguments);
      Mails.verify(lcUsername, lcEmail, verifyURL);
    }
    return result;
  }
}
