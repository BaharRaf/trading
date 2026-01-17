package net.froihofer.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class to perform JNDI lookups with Wildfly. Initialize
 * and instance with a correctly configured {@link InitialContext} and the
 * details about application, module, and distinctName as applicable.
 * For details see the JBoss documentation <a href="https://docs.jboss.org/author/display/AS71/EJB+invocations+from+a+remote+client+using+JNDI">
 * here</a> and <a href="https://docs.jboss.org/author/display/AS71/Remote+EJB+invocations+via+JNDI+-+EJB+client+API+or+remote-naming+project">here</a>.
 * @author Lorenz Froihofer
 * @version $Id$
 */
public class WildflyJndiLookupHelper {

  private static final Logger log = LoggerFactory.getLogger(WildflyJndiLookupHelper.class);
  private InitialContext ctx;
  private String applicationName = "";
  private String moduleName = "";
  private String distinctName = "";

  /**
   * Creates a new instance based on the provided parameters using the given {@link InitialContext}
   * for JNDI lookups.
   * @param applicationName Name of the EAR file or the application-name if
   * overridden in the application.xml deployment descriptor. Is empty, if
   * the application is not deployed in an EAR file.
   * @param moduleName Name of the JAR file containing the EJBs or the
   * module-name if overridden in the ejb-jar.xml deployment descriptor.
   * @param distinctName Usually an empty string - unless a distinct name has
   * been specified for the deployment
   */
  public WildflyJndiLookupHelper(InitialContext initialContext, String applicationName, String moduleName, String distinctName) {
    this.ctx = initialContext;
    this.applicationName = applicationName;
    this.moduleName = moduleName;
    this.distinctName = distinctName;
  }

  private <T> String getJndiName(String beanName, Class<T> remoteInterfaceClass) {
    return applicationName + "/" +moduleName+"/"+distinctName+ "/"+beanName+"!"+remoteInterfaceClass.getName();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T lookupUsingJBossEjbClient(String beanName, Class<T> remoteInterfaceClass, boolean isStateless) throws NamingException {
    return (T) ctx.lookup("ejb:" + getJndiName(beanName, remoteInterfaceClass) +(isStateless ? "" : "?stateful"));
  }
  
  @SuppressWarnings("unchecked")
  public <T> T lookup(String beanName, Class<T> remoteInterfaceClass) throws NamingException {
    return (T) ctx.lookup(getJndiName(beanName, remoteInterfaceClass));
  }
  
  public Object lookup(String jndiName) throws NamingException {
    return ctx.lookup(jndiName);
  }
}
