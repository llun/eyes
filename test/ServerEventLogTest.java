import java.util.Date;

import models.Server;
import models.ServerEventLog;
import models.Server.Status;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ServerEventLogTest extends UnitTest {

  @Before
  public void setup() {
    Fixtures.deleteAll();
    Fixtures.load("test.yml");
  }

  @Test
  public void testCountWithStatusAndDate() {
    Server server = Server.all().first();
    ServerEventLog.count(server, Status.UP, new Date(), new Date());
  }

  @Test
  public void testCountWithDate() {

  }

  @Test
  public void testCountWithStatus() {

  }

  @Test
  public void testCount() {

  }

}
