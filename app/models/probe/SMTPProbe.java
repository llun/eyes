package models.probe;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import models.Server;

import play.Logger;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;
import play.libs.Mail.SMTPAuthenticator;

@Entity
public class SMTPProbe extends Model implements Probe {

  public static final String TYPE = "SMTP";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String recipient;
  public String sender;
  public String username;
  public String password;
  public String address;
  
  public Boolean disable;

  @Lob
  public ProbeResult status;

  public SMTPProbe(Server server, String name, String recipient, String sender,
      String username, String password, String address) {
    this.server = server;
    this.name = name;
    this.recipient = recipient;
    this.sender = sender;
    this.username = username;
    this.password = password;
    this.address = address;
  }

  public String type() {
    return TYPE;
  }

  public String name() {
    return name;
  }

  public Server server() {
    return server;
  }

  public Boolean disable() {
    return disable;
  }
  
  public ProbeResult status() {
    return status;
  }

  public ProbeResult check() {
    ProbeResult result;

    Properties properties = new Properties();
    properties.put("mail.smtp.host", address);
    properties.put("mail.smtp.port", "25");
    properties.put("mail.smtp.auth", "true");

    Session session = Session.getInstance(properties, new SMTPAuthenticator(
        username, password));
    MimeMessage message = new MimeMessage(session);

    try {
      InternetAddress from = new InternetAddress(this.sender);
      InternetAddress recipients[] = { new InternetAddress(recipient) };
      String subject = String.format("Test send mail from %s", address);

      message.setReplyTo(new InternetAddress[] { from });
      message.setRecipients(RecipientType.TO, recipients);
      message.setFrom(from);
      message.setSubject(subject);
      message.setText("Test message");
      message.setSentDate(new Date());
      Transport.send(message);

      result = new ProbeResult(true);
    } catch (Exception e) {
      String error = String.format("Can't send mail from % server.", address);
      Logger.error(e, error);
      result = new ProbeResult(false, error);
    }
    return result;
  }

}
