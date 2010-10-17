package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import models.probe.Probe;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.Java;

@With(Secure.class)
public class Probes extends Controller {

  public static void disable(Long server, String type, Long probe, boolean value) {
    try {
      Class clazz = Play.classloader.loadClass(String.format(
          "models.probe.%sProbe", type));
      Method method = clazz.getMethod("findById", Object.class);
      Probe object = (Probe) Java.invokeStatic(method, new Object[] { probe });

      Field field = object.getClass().getField("disable");
      field.set(object, value);

      method = object.getClass().getMethod("save");
      method.invoke(object);
    } catch (Exception e) {
      Logger.error(e, "Can't load %s probe", type);
    }

    Servers.show(server);
  }

}
