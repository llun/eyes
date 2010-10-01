import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import models.Server;
import models.Server.Status;
import models.ServerEventLog;

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
  public void testCountWithStatusAndDateRange() throws ParseException {
    Server server = Server.all().first();
    DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date begin = format.parse("Sep 14, 2010");
    Date end = format.parse("Sep 16, 2010");
    long count = ServerEventLog.eventCount(server, Status.UP, begin, end);
    assertEquals(1, count);
    
    count = ServerEventLog.eventCount(server, Status.DOWN, begin, end);
    assertEquals(2, count);
  }

  @Test
  public void testCountWithDateRange() throws ParseException {
    Server server = Server.all().first();
    DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date begin = format.parse("Sep 14, 2010");
    Date end = format.parse("Sep 16, 2010");
    long count = ServerEventLog.eventCount(server, begin, end);
    assertEquals(3, count);
  }

  @Test
  public void testCountWithStatusAndDate() throws ParseException {
    Server server = Server.all().first();
    DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date date = format.parse("Sep 14, 2010");
    long count = ServerEventLog.eventCount(server, Status.UP, date);
    assertEquals(1, count);

    count = ServerEventLog.eventCount(server, Status.DOWN, date);
    assertEquals(0, count);
  }

  @Test
  public void testCountWithDate() throws ParseException {
    Server server = Server.all().first();
    DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date date = format.parse("Sep 14, 2010");
    long count = ServerEventLog.eventCount(server, date);
    assertEquals(1, count);

    date = format.parse("Sep 13, 2010");
    count = ServerEventLog.eventCount(server, date);
    assertEquals(0, count);
  }

  @Test
  public void testCountWithStatus() {
    Server server = Server.all().first();
    long count = ServerEventLog.eventCount(server, Status.UP);
    assertEquals(3, count);

    count = ServerEventLog.eventCount(server, Status.DOWN);
    assertEquals(2, count);
  }

  @Test
  public void testCount() {
    Server server = Server.all().first();
    long count = ServerEventLog.eventCount(server);
    assertEquals(5, count);
  }

}
