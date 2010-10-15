import java.util.List;
import java.util.Set;

import models.Server;
import models.probe.HTTPProbe;
import models.probe.Probe;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ServerTest extends UnitTest {

  @Before
  public void setup() {
    Fixtures.deleteAll();
    Fixtures.load("test.yml");
  }

  @Test
  public void testProbe() {
    Server server = Server.find("byName", "sample").first();
    Probe[] probes = server.probes();
    assertEquals(2, probes.length);
  }

  @Test
  public void testProbeType() {
    String[] types = Server.probeTypes();
    assertEquals(5, types.length);
  }

  @Test
  public void testAllProbes() {
    Probe[] probes = Server.allProbes();
    assertEquals(2, probes.length);
  }
  
  @Test
  public void testActibeProbes() {
    Probe[] probes = Server.activeProbes();
    assertEquals(1, probes.length);
  }

  @Test
  public void testDestroy() {
    long totalProbe = HTTPProbe.count();
    assertEquals(2, totalProbe);

    Server server = Server.find("byName", "sample").first();
    server.destroy();

    totalProbe = HTTPProbe.count();
    assertEquals(0, totalProbe);
  }

  @Test
  public void testCreate() {
    Server server = Server.create("admin", "new server");
    assertNotNull(server);

    server = Server.create("admin", "new server");
    assertNull(server);
  }

  @Test
  public void testForUser() {
    Set<Server> servers = Server.forUser("admin");
    assertEquals(1, servers.size());

    servers = Server.forUser("guest");
    assertEquals(1, servers.size());
  }

  @Test
  public void testResponders() {
    List<Server> servers = Server.all().fetch();
    Server server = servers.get(0);
    assertEquals(1, server.activeResponders().length);

  }
  
}
