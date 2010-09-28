package models.probe;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server;

import play.Logger;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

@Entity
public class SSHLoginProbe extends Model implements Probe {

  public static final String TYPE = "SSHLogin";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String username;
  public String password;
  public String address;
  public Boolean status;

  public SSHLoginProbe(Server server, String name, String username,
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

    JSch jsch = new JSch();
    try {
      com.jcraft.jsch.Session session = jsch.getSession(username, address, 22);
      try {
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(1000);

        result = true;
      } catch (Exception e) {
        Logger.error(e, "Can't login ssh");
      } finally {
        session.disconnect();
      }

    } catch (JSchException e) {
      e.printStackTrace();
    }
    return result;
  }

}
