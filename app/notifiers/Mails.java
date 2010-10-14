package notifiers;

import java.util.ArrayList;
import java.util.Set;

import models.Server;
import models.User;
import play.Play;
import play.i18n.Messages;
import play.mvc.Mailer;

public class Mails extends Mailer {

  public static void alert(Server server) {
    if (server.probes().length > 0 && server.responders.size() > 0) {
      Set<User> responders = server.responders;
      for (User responder : responders) {
        addRecipient(responder.email);
      }

      setFrom(Play.configuration.getProperty("eyes.mail"));
      setSubject("Some probes in %s fails", server.name);
      send(server);
    }

  }

  public static void verify(String username, String to, String verifyURL) {
    setFrom(Play.configuration.getProperty("eyes.mail"));
    setSubject(Messages.get("mail.verify.head"));
    addRecipient(to);
    send(username, verifyURL);
  }

  public static void invite(Server server, String to, String inviteURL) {
    setFrom(Play.configuration.getProperty("eyes.mail"));
    setSubject(Messages.get("responder.invite.header", server.owner.username,
        server.name));
    addRecipient(to);

    send(server, inviteURL);
  }

}
