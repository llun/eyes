import models.probe.IMAPProbe;
import models.probe.ProbeResult;

import org.junit.Test;

import play.test.UnitTest;

public class IMAPProbeTest extends UnitTest {

  @Test
  public void testCheck() {

    IMAPProbe probe = new IMAPProbe(null, null, "admin@eyes.labs",
        "password", "mail.eyes.labs");
    ProbeResult result = probe.check();
    assertTrue(result.success);

  }

}
