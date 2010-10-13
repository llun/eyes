package models.probe;

import java.io.Serializable;

public class ProbeResult implements Serializable {

  public final String message;
  public final Boolean success;

  public ProbeResult(Boolean success) {
    this.success = success;
    this.message = "";
  }

  public ProbeResult(Boolean success, String message) {
    this.success = success;
    this.message = message;
  }

}
