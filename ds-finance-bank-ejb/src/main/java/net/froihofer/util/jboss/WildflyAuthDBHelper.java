package net.froihofer.util.jboss;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * This file allows to add users to the JBoss authentication database. Data
 * are stored in the application-users.properties and 
 * application-roles.properties files.
 * 
 * @author Lorenz Froihofer
 * @version $Id$
 */
public class WildflyAuthDBHelper {
  private static final Logger log = LoggerFactory.getLogger(WildflyAuthDBHelper.class);
  private static final String NEWLINE = System.getProperty("line.separator");

  private File jbossHomePath;
  private File applicationUsersDbStandalone;
  private File rolesDbStandalone;
  private File applicationUsersDbDomain;
  private File rolesDbDomain;


  /**
   * Constructs a new instance based on the JBOSS_HOME environment variable.
   * @throws IllegalStateException if the JBOSS_HOME environment variable is not set.
   * @throws IllegalArgumentException if the location does not seem to be a JBoss location.
   */
  public WildflyAuthDBHelper() {
    String jbossHome = System.getenv("JBOSS_HOME");
    if (jbossHome == null) throw new IllegalStateException("JBOSS_HOME is not set");
    init(new File(jbossHome));
  }

  /**
   * Constructs a new instance with the path pointing
   * to a JBOSS_HOME location.
   * @param jbossHomePath path to the JBOSS_HOME location.
   * @throws IllegalArgumentException if the location does not seem to be a JBoss location.
   */
  public WildflyAuthDBHelper(File jbossHomePath) {
    init (jbossHomePath);
  }

  private void init(File jbossHomePath) {
    if (!jbossHomePath.exists() ||
            ! new File (jbossHomePath.getAbsolutePath()+"/standalone").exists() ||
            ! new File (jbossHomePath.getAbsolutePath()+"/domain").exists()) {
      throw new IllegalArgumentException("\""+jbossHomePath.getAbsolutePath()+"\""
              + " does not seem to be a Wildfly location.");
    }
    this.jbossHomePath = jbossHomePath;
    this.applicationUsersDbStandalone = new File(jbossHomePath.getAbsolutePath()+"/standalone/configuration/application-users.properties");
    this.rolesDbStandalone = new File(jbossHomePath.getAbsolutePath()+"/standalone/configuration/application-roles.properties");
    this.applicationUsersDbDomain = new File(jbossHomePath.getAbsolutePath()+"/domain/configuration/application-users.properties");
    this.rolesDbDomain = new File(jbossHomePath.getAbsolutePath()+"/domain/configuration/application-roles.properties");
  }

  private String getInitialCommentFromPropertiesFile(File propsFile) throws IOException {
    String lastLine;
    List<String> lines;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propsFile)))) {
      String line = StringUtils.trim(br.readLine());
      boolean stillComment = line.startsWith("#");
      line = line.replaceFirst("#", "");
      String result = "";
      lastLine = line;
      lines = new ArrayList<>();
      while (stillComment && line != null) {
        lines.add(line);
        lastLine = line;
        line = StringUtils.trim(br.readLine());
        stillComment = line != null && line.startsWith("#");
      }
    }
    if (lastLine.matches("#(Sun|Mon|Tue|Wed|Thu|Fri|Sat) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec).*")) {
      lines.remove(lines.size()-1);
    }
    return StringUtils.join(lines.iterator(),NEWLINE);
  }
  
  private void addUserToDBs(String user, String password, String[] roles, File usersDb, File rolesDb) throws IOException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new IOException(e);
    }
    //Process usersDB
    String initialComment = getInitialCommentFromPropertiesFile(usersDb);
    Properties users = new Properties();
    InputStreamReader isr = new InputStreamReader(new FileInputStream(usersDb));
    users.load(isr);
    isr.close();
    String passwordEntry = Hex.encodeHexString(md.digest((user+":ApplicationRealm:"+password).getBytes())).toLowerCase();
    users.put(user, passwordEntry);
    PrintWriter pw = new PrintWriter(usersDb);
    users.store(pw,initialComment);
    pw.close();
    
    // Process rolesDB
    Properties rolesProps = new Properties();
    isr = new InputStreamReader(new FileInputStream(rolesDb));
    rolesProps.load(isr);
    isr.close();
    rolesProps.put(user, StringUtils.join(roles,","));
    pw = new PrintWriter(rolesDb);
    rolesProps.store(pw,initialComment);
    pw.close();
  }
  
  /**
   * Adds a user to the JBoss authentication database.
   * @param user username of the user
   * @param password password of the user
   * @param roles roles to be assigned to the user
   * @throws IOException if errors pop up during file access
   */
  public void addUser(String user, String password, String[] roles) throws IOException {
    addUserToDBs(user,password,roles,applicationUsersDbStandalone,rolesDbStandalone);
    //addUserToDBs(user,password,roles,applicationUsersDbDomain,rolesDbDomain);
  }
  
  public void removeUser(String user) {
    throw new UnsupportedOperationException("removeUser(user) is left as an exercise");
  }
  
}
