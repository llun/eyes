package models.probe;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server;

import play.Logger;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;

@Entity
public class IMAPProbe extends Model implements Probe {

  public static final String TYPE = "IMAP";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String username;
  public String password;
  public String address;
  public Boolean status;
  
  public IMAPProbe(Server server, String name, String username,
      String password, String address) {
    this.server = server;
    this.name = name;
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
  
  public boolean status() {
    return status;
  }

  public boolean check() {
    boolean result = false;

    Properties properties = new Properties();
    properties.setProperty("mail.store.protocol", "imap");
    Session session = Session.getInstance(properties, null);
    try {
      Store store = session.getStore("imap");
      try {
        Logger.info("Checking IMAP: %s", address);
        store.connect(address, username, password);
        store.getDefaultFolder();
        result = true;
      } catch (Exception e) {
        Logger.error(e, "Can't connect to IMAP");
      } finally {
        store.close();
      }
    } catch (Exception e) {
      Logger.error(e, "Can't connect to IMAP");
      result = false;
    }
    return result;
  }

}
