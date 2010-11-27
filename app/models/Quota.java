package models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import play.db.jpa.Model;

@Entity
public class Quota extends Model {

  @OneToOne
  @JoinColumn(name = "user_id")
  public User user;

  public Integer serverLimit;
  public Integer probeLimit;
  public Integer responderLimit;

  public Boolean allowMSN;

  public String allowProbes;

  public Quota(User user) {
    this.user = user;

    serverLimit = -1;
    probeLimit = -1;
    responderLimit = -1;

    allowMSN = false;
  }

  public void addProbe(String probe) {
    Set<String> probes = allowProbes();
    allowProbes = StringUtils.join(probes.toArray(new String[probes.size()]),
        ",");
    save();
  }

  public Set<String> allowProbes() {
    HashSet<String> probes = new HashSet<String>();
    CollectionUtils.addAll(probes, allowProbes.split(","));
    return probes;
  }

  public boolean canCreateServer() {
    boolean result = false;
    if (serverLimit < 0) {
      result = true;
    } else {
      long count = Server.count("byOwner", user);
      result = count < serverLimit;
    }
    return result;
  }

  public boolean canCreateProbe(Server server) {
    boolean result = false;
    if (probeLimit < 0) {
      result = true;
    } else {
      result = server.probes().length < probeLimit;
    }
    return result;

  }

  public boolean canInviteResponder(Server server) {
    boolean result = false;
    if (probeLimit < 0) {
      result = true;
    } else {
      Invite[] invites = Invite.forServer(server);
      result = server.responders.size() + invites.length < responderLimit;
    }

    return result;
  }

  public static Quota forUsername(String username) {
    return Quota.find("byUser", User.fromUsername(username)).first();
  }

  public static boolean allowCreateProbe(User user, String probeType) {
    Quota quota = Quota.find("byUser", user).first();
    return quota.allowProbes().contains(probeType);
  }

  public static boolean canCreateServer(User user) {
    Quota quota = Quota.find("byUser", user).first();
    return quota.canCreateServer();
  }

  public static boolean canCreateProbe(User user, Server server) {
    Quota quota = Quota.find("byUser", user).first();
    return quota.canCreateProbe(server);
  }

  public static boolean canInviteResponder(User user, Server server) {
    Quota quota = Quota.find("byUser", user).first();
    return quota.canInviteResponder(server);
  }
  
  public static Quota createDefaultQuota(User user) {
    Quota quota = new Quota(user);
    quota.serverLimit = 1;
    quota.probeLimit = 3;
    quota.responderLimit = 3;
    quota.allowMSN = false;
    quota.allowProbes = "HTTP,HTTPForm,IMAP";
    return quota;
  }

}
