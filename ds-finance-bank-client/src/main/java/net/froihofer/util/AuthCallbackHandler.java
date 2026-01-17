package net.froihofer.util;

import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//

/**
 *
 * @author Lorenz Froihofer
 * @version $Id$
 */
public class AuthCallbackHandler implements CallbackHandler {
  private static final Logger log = LoggerFactory.getLogger(AuthCallbackHandler.class);

  private static String username;
  private static String password;
  private static String realm = "ApplicationRealm";

  public static String getUsername() {
    return username;
  }

  public static void setUsername(String aUsername) {
    username = aUsername;
  }

  public static String getPassword() {
    return password;
  }

  public static void setPassword(String aPassword) {
    password = aPassword;
  }

  public static String getRealm() {
    return realm;
  }

  public static void setRealm(String aRealm) {
    realm = aRealm;
  }
  
  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    if (callbacks == null) return;
    log.debug("Received callbacks: "+Arrays.asList(callbacks));
    
    //Unfortunately, querying has to be done before the callback
    //arrives as otherwise JBoss remoting runs into a timeout.
    //Due to the constraints imposed by JBoss remoting, we use static
    //variables to store the credentials.
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        ((NameCallback)callback).setName(getUsername());
      }
      else if (callback instanceof PasswordCallback) {
        ((PasswordCallback)callback).setPassword(getPassword().toCharArray());
      }
      else if (callback instanceof RealmCallback) {
        ((RealmCallback)callback).setText(getRealm());
      }
      else {
        log.warn("Received unsupported callback type: "+callback.getClass().getName());
      }
    }
  }

}
