package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server.Status;
import play.db.jpa.Model;

@Entity
public class EventLog extends Model {

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  @Enumerated(EnumType.ORDINAL)
  public Status status;
  public String message;
  public Date created;

}
