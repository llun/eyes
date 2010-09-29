package models;

import play.db.jpa.Model;

public class ProbeEventLog extends Model {

  public Long probeID;
  public String type;
  public Boolean status;
  public String message;

  public ProbeEventLog(Long probeID, String type, boolean status, String message) {
    this.probeID = probeID;
    this.type = type;
    this.status = status;
    this.message = message;
  }

  public static void submit(Long probeID, String type, boolean status,
      String message) {
    ProbeEventLog event = new ProbeEventLog(probeID, type, status, message);
    event.save();
  }

}
