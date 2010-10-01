import models.probe.HTTPProbe;

import org.junit.Test;

import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.test.UnitTest;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

public class HTTPProbeTest extends UnitTest {

  @Test
  public void testCheck() {
    HTTPProbe probe = new HTTPProbe(null, null, "http://llun.in.th/", 200);
    Boolean result = probe.check();
    assertTrue(result);

    probe = new HTTPProbe(null, null, "https://google.com/", 200);
    result = probe.check();
    assertTrue(result);
  }

}
