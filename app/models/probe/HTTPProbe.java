package models.probe;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import play.Logger;
import play.db.jpa.Model;
import clients.YesSSLProtocolSocketFactory;

@Entity
public class HTTPProbe extends Model implements Probe {

  public static final String TYPE = "HTTP";

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public String name;
  public String serverURL;
  public Integer expectResponse;
  public Boolean status;

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

  public boolean status() {
    return status;
  }

  public boolean check() {
    boolean result = false;

    URI serverURI = URI.create(serverURL);
    String scheme = serverURI.getScheme();

    if (scheme.toLowerCase().equals("https")) {
      int port = serverURI.getPort();
      port = port == -1 ? 443 : port;
      Protocol.registerProtocol("https", new Protocol("https",
          new YesSSLProtocolSocketFactory(), port));
    }

    HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();

    HttpClient client = new HttpClient(connectionManager);
    client.getParams().setParameter("http.useragent", "Test Client");
    client.getParams().setConnectionManagerTimeout(1000);
    client.getParams().setSoTimeout(1000);

    GetMethod get = new GetMethod(serverURL);

    try {
      int status = client.executeMethod(get);

      if (status == expectResponse) {
        result = true;
      }

    } catch (Exception e) {
      Logger.error(e, "Can't get data from %s", serverURL);
    } finally {
      get.releaseConnection();
    }
    return result;
  }

}
