package net.froihofer.dsfinance.bank.service;

import jakarta.ejb.Remote;

/**
 * Remote interface for authentication verification.
 * Provides a simple way to check if authentication works without login() methods.
 * 
 * This is used to verify container-managed security is working correctly.
 */
@Remote
public interface AuthenticationCheckService {
    
    /**
     * Ping method to verify authentication.
     * Returns the authenticated username.
     * 
     * Accessible to both employees and customers.
     * 
     * @return Username of the authenticated caller
     */
    String checkAuthentication();
    
    /**
     * Verifies if the caller has a specific role.
     * 
     * @param role Role name to check ("employee" or "customer")
     * @return true if caller has the role, false otherwise
     */
    boolean hasRole(String role);
}
