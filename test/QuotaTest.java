import models.Quota;
import models.Server;
import models.User;
import models.probe.HTTPProbe;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class QuotaTest extends UnitTest {

  @Before
  public void setup() {
    Fixtures.deleteAll();
    Fixtures.load("test.yml");
  }

  @Test
  public void testAllowCreateProbe() {
    User user = User.find("byUsername", "onelimit").first();
    boolean result = Quota.allowCreateProbe(user, "HTTP");
    assertTrue(result);

    result = Quota.allowCreateProbe(user, "HTTPForm");
    assertFalse(result);
  }

  @Test
  public void testCanCreateServer() {
    User user = User.find("byUsername", "onelimit").first();
    boolean result = Quota.canCreateServer(user);
    assertTrue(result);

    Server.create(user.username, "onelimit server");
    result = Quota.canCreateServer(user);
    assertFalse(result);

    user = User.find("byUsername", "admin").first();
    result = Quota.canCreateServer(user);
    assertTrue(result);

    Server.create(user.username, "new admin");
    result = Quota.canCreateServer(user);
    assertTrue(result);
  }

  @Test
  public void testCanCreateProbe() {
    User user = User.find("byUsername", "onelimit").first();
    Server server = Server.create(user.username, "one limit server");
    boolean result = Quota.canCreateProbe(user, server);
    assertTrue(result);

    HTTPProbe probe = new HTTPProbe(server, "New probe", "http://llun.in.th",
        200);
    probe.save();

    result = Quota.canCreateProbe(user, server);
    assertFalse(result);

  }

  @Test
  public void testCanCreateResponder() {
    User user = User.find("byUsername", "onelimit").first();
    Server server = Server.create(user.username, "one limit server");
    boolean result = Quota.canInviteResponder(user, server);
    assertTrue(result);

    User responder = User.find("byUsername", "admin").first();
    server.addResponder(responder);

    result = Quota.canInviteResponder(user, server);
    assertFalse(result);

  }

}
