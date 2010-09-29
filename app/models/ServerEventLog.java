package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server.Status;
import play.db.jpa.Model;

@Entity
public class ServerEventLog extends Model {

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public Date created;
  public Status status;
  public String message;

  public ServerEventLog(Server server, Status status, String message) {
    this.server = server;
    this.status = status;
    this.message = message;
    this.created = new Date();
  }

  public static void submit(Server server, Status status, String message) {
    ServerEventLog event = new ServerEventLog(server, status, message);
    event.save();
  }

}
