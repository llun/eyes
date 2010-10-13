import java.util.HashMap;

import models.probe.HTTPFormProbe;
import models.probe.ProbeResult;

import org.junit.Test;

import play.test.UnitTest;

public class HTTPFormProbeTest extends UnitTest {

  @Test
  public void testCheck() {
    // Not found for sure
    HashMap<String, String> properties = new HashMap<String, String>();
    properties.put("username", "username");
    properties.put("password", "password");

    HTTPFormProbe probe = new HTTPFormProbe(null, null, "http://eyes.labs",
        200, properties);
    ProbeResult result = probe.check();
    assertTrue(result.success);
  }

}
