package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import play.db.jpa.Model;

@Entity
public class EventLog extends Model {

  public static enum Type {
    Server, Probe;
  }

  // Event creates from server status or probe status
  @Enumerated(EnumType.ORDINAL)
  public Type type;

  // Event instance, server id or probe id
  public Long instance;

  // Event status, e.g. 0 for server means fail and 1 for server means ok.
  public Integer status;

  // Event message
  public String message;
  public Date created;

  public EventLog(Type type, Long instance, Integer status, String message) {
    this.type = type;
    this.instance = instance;
    this.status = status;
    this.message = message;
    this.created = new Date();
  }

  public static void submit(Type type, Long instance, Integer status) {
    submit(type, instance, status, "");
  }

  public static void submit(Type type, Long instance, Integer status,
      String message) {
    EventLog event = new EventLog(type, instance, status, message);
    event.save();
  }

}
