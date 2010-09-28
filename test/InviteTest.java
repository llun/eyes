import models.Invite;
import models.Invite.Result;
import models.Server;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class InviteTest extends UnitTest {

  @Before
  public void setup() {
    Fixtures.deleteAll();
    Fixtures.load("test.yml");
  }

  @Test
  public void testInviteResponder() {
    Server server = Server.all().first();
    boolean success = Invite.inviteResponder(server, "ender@freeworld.net");
    assertTrue(success);

    success = Invite.inviteResponder(server, "admin@eyes.labs");
    assertFalse(success);

    success = Invite.inviteResponder(server, "guest@eyes.labs");
    assertFalse(success);
  }

  @Test
  public void testAcceptResponder() {
    Server server = Server.all().first();
    Result result = Invite.acceptResponder(server.id, "noserver@eyes.labs",
        "code");
    assertEquals(Result.OK, result);

    result = Invite.acceptResponder(server.id, "nouser@eyes.labs", "code");
    assertEquals(Result.NOUSER, result);

    result = Invite.acceptResponder(server.id, "noserver@eyes.labs", "wrongcode");
    assertEquals(Result.REJECT, result);
    
    result = Invite.acceptResponder(server.id, "anonymous@eyes.labs", "code");
    assertEquals(Result.REJECT, result);
  }

}
