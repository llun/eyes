package models.probe;

import models.Server;

public interface Probe {
  
  String type();
  String name();
  Server server();
  boolean status();
  boolean check();
  
}
