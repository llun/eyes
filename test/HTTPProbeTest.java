import models.probe.HTTPProbe;
import models.probe.ProbeResult;

import org.junit.Test;

import play.test.UnitTest;

public class HTTPProbeTest extends UnitTest {

  @Test
  public void testCheck() {
    HTTPProbe probe = new HTTPProbe(null, null, "http://llun.in.th/", 200);
    ProbeResult result = probe.check();
    assertTrue(result.success);

    probe = new HTTPProbe(null, null, "https://google.com/", 200);
    result = probe.check();
    assertTrue(result.success);
  }

}
