package models;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.User.Role;
import notifiers.Mails;
import play.db.jpa.Model;
import play.libs.Codec;
import play.mvc.Router;

@Entity
public class Invite extends Model {

  public static enum Result {
    OK, NOUSER, REJECT;
  }

  @JoinColumn(name = "server_id")
  @OneToOne
  public Server server;
  public String email;
  public String code;
  public Date created;

  public Invite(Server server, String email, String code) {
    this.server = server;
    this.email = email;
    this.code = code;
    this.created = new Date();
  }

  public static Invite[] forServer(Server server) {
    List<Invite> invites = Invite.find("byServer", server).fetch();
    return invites.toArray(new Invite[invites.size()]);
  }

  public static boolean inviteResponder(Server server, String email) {
    boolean result = false;
    String lcEmail = email.toLowerCase();

    long foundEmail = Invite.count("byServerAndEmail", server, lcEmail);
    if (foundEmail == 0) {
      User user = User.fromEmail(lcEmail);
      Set<User> users = server.responders;
      if (!users.contains(user) && !server.owner.equals(user)) {

        String inviteCode = Codec.hexMD5(Codec.UUID());
        Invite invite = new Invite(server, lcEmail, inviteCode);
        invite.save();

        HashMap<String, Object> arguments = new HashMap<String, Object>(3);
        arguments.put("server", server.id);
        arguments.put("email", lcEmail);
        arguments.put("code", inviteCode);
        String inviteURL = Router.getFullUrl("Invites.invite", arguments);

        Mails.invite(server, lcEmail, inviteURL);
        result = true;
      }

    }

    return result;
  }

  public static Result acceptResponder(Long server, String email, String code) {
    Result result = Result.REJECT;
    Server instance = Server.findById(server);
    Invite invite = Invite.find("byServerAndEmailAndCode", instance, email,
        code).first();
    if (invite != null) {
      User user = User.find("byEmail", email).first();
      if (user != null) {
        instance.addResponder(user);
        invite.delete();
        result = Result.OK;
      } else {
        result = Result.NOUSER;
      }
    }
    return result;
  }

  public static boolean registerResponder(Long server, String username,
      String password, String code) {
    boolean result = false;
    Server instance = Server.findById(server);
    Invite invite = Invite.find("byServerAndCode", instance, code).first();
    if (invite != null) {
      if (invite.code.equals(code)) {
        User user = User.find("username like ? or email like ?", username,
            invite.email).first();
        if (user == null) {
          user = new User(username, password, invite.email);
          user.active = true;
          user.verifyCode = "";
          user.role = Role.RESPONDER;
          user.save();
          
          Quota quota = Quota.createDefaultQuota(user);
          quota.save();

          instance.addResponder(user);
          instance.save();

          invite.delete();

          result = true;
        }
      }
    }

    return result;
  }
}
