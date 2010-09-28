package models.probe;

import java.net.URI;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import models.Server;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import play.Logger;
import play.db.jpa.Model;
import clients.YesSSLProtocolSocketFactory;

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

    PostMethod post = new PostMethod(serverURL);
    for (String key : properties.keySet()) {
      post.addParameter(key, properties.get(key));
    }

    try {
      int status = client.executeMethod(post);

      if (status == expectResponse) {

        Header[] headers = post.getResponseHeaders();
        for (Header header : headers) {
          Logger.info("%s: %s", header.getName(), header.getValue());
        }

        result = true;
      }

    } catch (Exception e) {
      Logger.error(e, "Can't login to %s", serverURI);
    } finally {
      post.releaseConnection();
    }
    return result;
  }

}
