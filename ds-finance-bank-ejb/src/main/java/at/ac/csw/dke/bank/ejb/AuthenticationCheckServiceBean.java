package at.ac.csw.dke.bank.ejb;

import at.ac.csw.dke.bank.service.AuthenticationCheckService;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;

/**
 * EJB implementation for authentication checking.
 * Demonstrates container-managed security without login() methods.
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class AuthenticationCheckServiceBean implements AuthenticationCheckService {
    
    @Resource
    private SessionContext sessionContext;
    
    @Override
    @RolesAllowed({"employee", "customer"})
    public String checkAuthentication() {
        // No login() method - container handles authentication!
        // We just retrieve the authenticated principal from SessionContext
        String username = sessionContext.getCallerPrincipal().getName();
        return "Authenticated as: " + username;
    }
    
    @Override
    @RolesAllowed({"employee", "customer"})
    public boolean hasRole(String role) {
        return sessionContext.isCallerInRole(role);
    }
}
