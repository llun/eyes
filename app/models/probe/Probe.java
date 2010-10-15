package models.probe;

import models.Server;

public interface Probe {
  
  Long getId();
  String type();
  String name();
  Server server();
  ProbeResult status();
  ProbeResult check();
  Boolean disable();
  
}
