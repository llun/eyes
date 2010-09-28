import models.probe.SSHLoginProbe;

import org.junit.Test;

import play.test.UnitTest;

public class SSHLoginProbeTest extends UnitTest {

  @Test
  public void testCheck() {

    SSHLoginProbe probe = new SSHLoginProbe(null, null, "username", "password",
        "eyes.labs");
    Boolean result = probe.check();
    assertTrue(result);

  }

}
