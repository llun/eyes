import models.probe.HTTPProbe;

import org.junit.Test;

import play.test.UnitTest;

public class HTTPProbeTest extends UnitTest {

  @Test
  public void testCheck() {
    HTTPProbe probe = new HTTPProbe(null, null, "http://llun.in.th", 200);
    Boolean result = probe.check();
    assertTrue(result);

    probe = new HTTPProbe(null, null, "https://google.com", 200);
    result = probe.check();
    assertTrue(result);
  }

}
