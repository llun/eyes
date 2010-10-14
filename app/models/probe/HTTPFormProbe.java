package models.probe;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Future;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig.Builder;

import models.Server;
import play.Logger;
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
  @Lob
  public ProbeResult status;
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

  public ProbeResult status() {
    return status;
  }

  public ProbeResult check() {
    ProbeResult result;

    Builder confBuilder = new AsyncHttpClientConfig.Builder();
    confBuilder.setRequestTimeoutInMs(1000);
    confBuilder.setConnectionTimeoutInMs(1000);
    confBuilder.setIdleConnectionTimeoutInMs(1000);
    confBuilder.setFollowRedirects(false);
    confBuilder.setKeepAlive(false);

    AsyncHttpClient client = new AsyncHttpClient(confBuilder.build());

    try {
      BoundRequestBuilder request = client.preparePost(serverURL);
      request.setHeader("Content-Type", "application/x-www-form-urlencoded");

      StringBuilder sb = new StringBuilder();
      for (String key : properties.keySet()) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        Object value = properties.get(key);

        if (value != null) {
          if (value instanceof Collection<?> || value.getClass().isArray()) {
            Collection<?> values = value.getClass().isArray() ? Arrays
                .asList((Object[]) value) : (Collection<?>) value;
            boolean first = true;
            for (Object v : values) {
              if (!first) {
                sb.append("&");
              }
              first = false;
              sb.append(WS.encode(key)).append("=")
                  .append(WS.encode(v.toString()));
            }
          } else {
            sb.append(WS.encode(key)).append("=")
                .append(WS.encode(properties.get(key).toString()));
          }
        }
      }
      request.setBody(sb.toString());

      Future<Response> future = request.execute();
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
