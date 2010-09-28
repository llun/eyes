package models.probe;

import models.Server;

public interface Probe {
  
  static final Integer FAIL = 0;
  static final Integer OK = 1;
  
  Long getId();
  String type();
  String name();
  Server server();
  boolean status();
  boolean check();
  
}
