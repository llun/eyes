package models.probe;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import models.Server;
import play.Logger;
import play.db.jpa.Model;
import play.libs.WS.HttpResponse;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

@Entity
public class HTTPProbe extends Model implements Probe {

  public static final String TYPE = "HTTP";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String serverURL;
  public Integer expectResponse;

  public Boolean disable;
  
  @Lob
  public ProbeResult status;

  public HTTPProbe(Server server, String name, String URL, int expectResponse) {
    this.server = server;
    this.name = name;
    this.serverURL = URL;
    this.expectResponse = expectResponse;
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

    Builder confBuilder = new AsyncHttpClientConfig.Builder();
    confBuilder.setRequestTimeoutInMs(5000);
    confBuilder.setConnectionTimeoutInMs(5000);
    confBuilder.setIdleConnectionTimeoutInMs(5000);
    confBuilder.setFollowRedirects(true);
    confBuilder.setKeepAlive(false);

    AsyncHttpClient client = new AsyncHttpClient(confBuilder.build());

    try {
      Future<Response> future = client.prepareGet(serverURL).execute();
      Response response = future.get();
      if (response.getStatusCode() == expectResponse) {
        result = new ProbeResult(true);
      } else {
        String message = String.format("Server response %d but expect %d",
            response.getStatusCode(), expectResponse);
        Logger.error(message);
        result = new ProbeResult(false, message);
      }
    } catch (Exception e) {
      String message = String.format("Server can't connect to %s", serverURL);
      Logger.error(e, message);
      result = new ProbeResult(false, message);
    }

    client.close();

    return result;
  }

}
