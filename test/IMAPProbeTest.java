import models.probe.IMAPProbe;

import org.junit.Test;

import play.test.UnitTest;

public class IMAPProbeTest extends UnitTest {

  @Test
  public void testCheck() {

    IMAPProbe probe = new IMAPProbe(null, null, "admin@eyes.labs",
        "password", "mail.eyes.labs");
    Boolean result = probe.check();
    assertTrue(result);

  }

}
