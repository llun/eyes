package models;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import models.probe.Probe;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.utils.Java;

@Entity
public class Server extends Model implements Comparable<Server> {

  public static enum Status {
    UP, DOWN;
  }

  @OneToOne
  @JoinColumn(name = "owner_id")
  public User owner;
  @Required
  public String name;
  @Enumerated(EnumType.ORDINAL)
  public Status status;
  public String message;
  @ManyToMany
  @JoinTable(name = "SERVER_USER", joinColumns = @JoinColumn(name = "SERVER_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
  public Set<User> responders;

  public Server(User owner, String name) {
    this.owner = owner;
    this.name = name;
    this.status = Status.DOWN;
    this.message = "";
    this.responders = new HashSet<User>();
  }

  public void addResponder(User user) {
    responders.add(user);
    save();
  }

  public void removeResponder(User user) {
    responders.remove(user);
    save();
  }

  public User[] activeResponders() {
    Predicate predicate = new Predicate() {
      public boolean evaluate(Object object) {
        boolean result = false;
        if (object instanceof User) {
          User user = (User) object;
          result = user.active;
        }
        return result;
      }
    };
    Collection<User> activeResponders = CollectionUtils.select(responders,
        predicate);
    return activeResponders.toArray(new User[activeResponders.size()]);
  }

  public String toString() {
    return name;
  }

  public int compareTo(Server other) {
    return name.compareToIgnoreCase(other.name);
  }

  public Probe[] probes() {
    HashSet<Probe> probes = new HashSet<Probe>();
    List<Class> founds = Play.classloader.getAssignableClasses(Probe.class);
    for (Class clazz : founds) {
      try {
        JPAQuery query = (JPAQuery) Java.invokeStatic(clazz, "find",
            "byServer", new Object[] { this });
        List<Probe> typedProbes = query.fetch();
        probes.addAll(typedProbes);
      } catch (Exception e) {
        Logger
            .error(e, "This probe(%s) isn't implement model", clazz.getName());
      }
    }

    return probes.toArray(new Probe[probes.size()]);
  }

  public void destroy() {
    // Destroy all probe.
    Probe[] probes = probes();
    for (Probe probe : probes) {
      if (probe instanceof Model) {
        Model model = (Model) probe;
        model.delete();
      }
    }

    // Destroy all invites.
    Invite.delete("server = ?", this);

    // Destroy all event logs.
    ServerEventLog.delete("server = ?", this);

    // destroy itself
    delete();
  }

  public static Collection<Probe> allProbes() {
    HashSet<Probe> probes = new HashSet<Probe>();
    List<Class> founds = Play.classloader.getAssignableClasses(Probe.class);
    for (Class clazz : founds) {
      try {
        JPAQuery query = (JPAQuery) Java.invokeStatic(clazz, "all");
        List<Probe> typedProbes = query.fetch();
        probes.addAll(typedProbes);
      } catch (Exception e) {
        Logger
            .error(e, "This probe(%s) isn't implement model", clazz.getName());
      }
    }

    return probes;
  }

  public static Collection<Probe> activeProbes() {
    Predicate predicate = new Predicate() {

      public boolean evaluate(Object object) {
        boolean active = false;
        if (object instanceof Probe) {
          Probe probe = (Probe) object;
          active = !(probe.disable() == null ? false : probe.disable());
        }
        return active;
      }
    };

    Collection<Probe> probes = CollectionUtils.select(allProbes(), predicate);
    return probes;
  }

  public static String[] probeTypes() {
    TreeSet<String> types = new TreeSet<String>();
    List<Class> founds = Play.classloader.getAssignableClasses(Probe.class);
    for (Class clazz : founds) {
      Field field;
      try {
        field = clazz.getDeclaredField("TYPE");
        Object object = field.get(clazz);
        types.add((String) object);
      } catch (Exception e) {
        Logger.error(e, "Can't get type from probe(%s)", clazz);
      }
    }
    return types.toArray(new String[types.size()]);
  }

  public static Set<Server> forUser(String username) {
    TreeSet<Server> servers = new TreeSet<Server>();

    User user = User.fromUsername(username);
    List<Server> ownServers = Server.find("byOwner", user).fetch();
    servers.addAll(ownServers);

    List<Server> responders = Server.find(
        "SELECT server FROM Server server WHERE ? MEMBER OF server.responders",
        user).fetch();
    servers.addAll(responders);

    return servers;
  }

  public static Server create(String owner, String name) {
    Server server = null;
    User user = User.fromUsername(owner);
    if (Quota.canCreateServer(user)) {
      server = Server.find("byOwnerAndName", user, name).first();
      if (server == null) {
        server = new Server(user, name);
        server.save();
      } else {
        server = null;
      }
    }
    return server;
  }

}
