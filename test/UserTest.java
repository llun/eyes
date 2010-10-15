import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class UserTest extends UnitTest {

  @Before
  public void setup() {
    Fixtures.deleteAll();
    Fixtures.load("test.yml");
  }

  @Test
  public void testAuthenticate() {
    boolean result = User.authenticate("admin", "password");
    assertTrue(result);

    result = User.authenticate("admin", "wrong password");
    assertFalse(result);

    result = User.authenticate("nouser", "password");
    assertFalse(result);
  }

  @Test
  public void testRegister() {
    boolean result = User.register("admin", "password", "admin@eyes.labs");
    assertFalse(result);

    result = User.register("nouser", "password", "admin@eyes.labs");
    assertFalse(result);

    result = User.register("nouser", "password", "nomail@eyes.labs");
    assertTrue(result);
  }

  @Test
  public void testVerify() {
    boolean result = User.authenticate("inactive", "password");
    assertFalse(result);

    User user = User.find("byUsername", "inactive").first();
    user.verify("code");

    result = User.authenticate("inactive", "password");
    assertTrue(result);
  }

  @Test
  public void testReject() {
    assertEquals(5, User.count());
    
    User user = User.find("byUsername", "inactive").first();
    user.reject("code");
    assertEquals(4, User.count());
  }

}
