import models.probe.SMTPProbe;

import org.junit.Test;

import play.test.UnitTest;

public class SMTPProbeTest extends UnitTest {

  @Test
  public void testCheck() {

    SMTPProbe probe = new SMTPProbe(null, null, "responders@eyes.labs",
        "admin@eyes.labs", "admin@eyes.labs", "password",
        "mail.eyes.labs");
    Boolean result = probe.check();
    assertTrue(result);
  }

}
