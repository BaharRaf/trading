/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.froihofer.dsfinance.bank.client;

// File: BankServiceConnector.java
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class BankServiceConnector {

    private EmployeeBankService employeeService;

    public void connect(String username, String password) throws Exception {
        // Set up authentication
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        props.put(Context.SECURITY_PRINCIPAL, username);
        props.put(Context.SECURITY_CREDENTIALS, password);

        // Create context and lookup EJB
        Context ctx = new InitialContext(props);

        // JNDI lookup (adjust based on your deployment)
        String jndiName = "ejb:/ds-finance-bank-ejb/EmployeeBankServiceBean!"
                + "net.froihofer.dsfinance.bank.api.EmployeeBankService";

        employeeService = (EmployeeBankService) ctx.lookup(jndiName);
    }

    public EmployeeBankService getEmployeeService() {
        return employeeService;
    }
}
