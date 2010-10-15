package models.probe;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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

  public Boolean disable;

  @Lob
  public ProbeResult status;

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

  public Boolean disable() {
    return disable;
  }

  public ProbeResult status() {
    return status;
  }

  public ProbeResult check() {
    ProbeResult result;
    JSch jsch = new JSch();
    try {
      com.jcraft.jsch.Session session = jsch.getSession(username, address, 22);
      try {
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(1000);

        result = new ProbeResult(true);
      } catch (Exception e) {
        String message = String.format("Can't ssh to %s with user %s", address,
            username);
        Logger.error(e, message);
        result = new ProbeResult(false, message);
      } finally {
        session.disconnect();
      }

    } catch (JSchException e) {
      String message = String.format("Can't create ssh session");
      Logger.error(e, message);
      result = new ProbeResult(false, message);
    }
    return result;
  }

}
