package models.probe;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import models.Server;
import play.db.jpa.Model;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

@Entity
public class HTTPFormProbe extends Model implements Probe {

  public static final String TYPE = "HTTPForm";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String serverURL;
  public Integer expectResponse;
  public Boolean status;
  @Lob
  public HashMap<String, String> properties;

  public HTTPFormProbe(Server server, String name, String URL,
      int expectResponse, HashMap<String, String> properties) {
    this.server = server;
    this.name = name;
    this.serverURL = URL;
    this.expectResponse = expectResponse;
    this.properties = properties;
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

    WSRequest request = WS.url(serverURL);
    request.setParameters(properties);
    HttpResponse response = request.post();
    if (response.getStatus().equals(expectResponse)) {
      result = true;
    }

    return result;
  }

}
