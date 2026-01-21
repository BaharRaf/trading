package net.froihofer.util.jboss;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

/**
 * Helper utility to programmatically add users to WildFly's ApplicationRealm.
 * This allows the banking application to create customer accounts that can
 * immediately authenticate via WildFly security.
 */
public class WildflyAuthDBHelper {

    private final File jbossHome;
    private final File applicationUsersPropertiesFile;
    private final File applicationRolesPropertiesFile;

    /**
     * Creates a helper for the given WildFly installation directory.
     * 
     * @param jbossHome The WildFly home directory (e.g., /opt/wildfly or C:\wildfly)
     */
    public WildflyAuthDBHelper(File jbossHome) {
        if (jbossHome == null || !jbossHome.exists() || !jbossHome.isDirectory()) {
            throw new IllegalArgumentException("Invalid WildFly home directory: " + jbossHome);
        }
        this.jbossHome = jbossHome;
        
        // Locate the properties files in standalone/configuration
        File configDir = new File(jbossHome, "standalone/configuration");
        this.applicationUsersPropertiesFile = new File(configDir, "application-users.properties");
        this.applicationRolesPropertiesFile = new File(configDir, "application-roles.properties");
        
        if (!applicationUsersPropertiesFile.exists()) {
            throw new IllegalStateException(
                "application-users.properties not found at: " + applicationUsersPropertiesFile.getAbsolutePath()
            );
        }
        if (!applicationRolesPropertiesFile.exists()) {
            throw new IllegalStateException(
                "application-roles.properties not found at: " + applicationRolesPropertiesFile.getAbsolutePath()
            );
        }
    }

    /**
     * Adds a new user to WildFly's ApplicationRealm with the specified roles.
     * If the user already exists, their password and roles will be updated.
     * 
     * @param username The username for authentication
     * @param password The plaintext password (will be hashed)
     * @param roles The roles to assign (e.g., "customer", "employee")
     * @throws IOException If file operations fail
     */
    public void addUser(String username, String password, String[] roles) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("At least one role must be specified");
        }

        // Hash the password using WildFly's expected format
        String hashedPassword = hashPassword(username, password);

        // Update application-users.properties
        addOrUpdateUserProperty(username, hashedPassword);

        // Update application-roles.properties
        addOrUpdateRolesProperty(username, roles);
    }

    /**
     * Hashes the password using WildFly's ApplicationRealm format:
     * HEX( MD5( username ':' realm ':' password ) )
     */
    private String hashPassword(String username, String password) {
        try {
            String realm = "ApplicationRealm"; // WildFly default realm name
            String data = username + ":" + realm + ":" + password;
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data.getBytes("UTF-8"));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Adds or updates a user entry in application-users.properties
     */
    private void addOrUpdateUserProperty(String username, String hashedPassword) throws IOException {
        Properties props = loadProperties(applicationUsersPropertiesFile);
        props.setProperty(username, hashedPassword);
        saveProperties(props, applicationUsersPropertiesFile, 
            "Updated by WildflyAuthDBHelper - User: " + username);
    }

    /**
     * Adds or updates role assignments in application-roles.properties
     */
    private void addOrUpdateRolesProperty(String username, String[] roles) throws IOException {
        Properties props = loadProperties(applicationRolesPropertiesFile);
        String rolesValue = String.join(",", roles);
        props.setProperty(username, rolesValue);
        saveProperties(props, applicationRolesPropertiesFile, 
            "Updated by WildflyAuthDBHelper - User: " + username);
    }

    /**
     * Loads a properties file safely
     */
    private Properties loadProperties(File file) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        }
        return props;
    }

    /**
     * Saves properties to a file with proper formatting
     */
    private void saveProperties(Properties props, File file, String comment) throws IOException {
        // Create backup before modifying
        File backupFile = new File(file.getAbsolutePath() + ".backup");
        Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, comment);
        }
    }

    /**
     * Removes a user from both properties files
     */
    public void removeUser(String username) throws IOException {
        Properties userProps = loadProperties(applicationUsersPropertiesFile);
        Properties roleProps = loadProperties(applicationRolesPropertiesFile);
        
        userProps.remove(username);
        roleProps.remove(username);
        
        saveProperties(userProps, applicationUsersPropertiesFile, 
            "Removed user: " + username);
        saveProperties(roleProps, applicationRolesPropertiesFile, 
            "Removed user: " + username);
    }

    /**
     * Checks if a user exists in the ApplicationRealm
     */
    public boolean userExists(String username) throws IOException {
        Properties props = loadProperties(applicationUsersPropertiesFile);
        return props.containsKey(username);
    }
}