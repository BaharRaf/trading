# Teil 2 Security Fixes - Instructor Feedback Implementation

## Date: January 21, 2026

## Overview

This document explains the critical changes made to address instructor feedback for Teil 2 (Phase 2) of the trading project. The main focus is on **container-managed security** and proper service design according to Java EE best practices.

---

## ❌ CRITICAL PROBLEM: Login Methods in Session Beans

### What Was Wrong (Original Design)

```java
// ❌ WRONG - Creates stateful beans!
public interface BankService {
    boolean login(String username, String password);  // DON'T DO THIS!
    void logout();
}
```

**Why This Is Wrong:**
- Having `login()` methods in session beans makes them **stateful**
- Each bean instance must store "logged-in" state
- This violates stateless session bean principles
- Difficult to scale and maintain
- Not how Java EE security is designed to work

### Instructor's Feedback

> "A dedicated login method in your session beans would make them stateful, because each bean would need to store 'logged-in' state. To avoid ending up with many stateful session beans, authentication should be moved out of your own business code into the container (identity store, validateUser, container-managed security). You generally do not need a business-level login() method."

---

## ✅ CORRECT SOLUTION: Container-Managed Security

### How Authentication Works Now

```java
@Stateless
@DeclareRoles({"employee", "customer"})
public class CustomerManagementServiceBean implements CustomerManagementService {
    
    @Resource
    private SessionContext sessionContext;  // Container provides this
    
    @RolesAllowed("employee")  // Container checks this automatically
    public long createCustomer(PersonDTO person) {
        // Get authenticated user from container
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        
        // NO LOGIN METHOD - Container already authenticated!
        // Business logic goes here
    }
}
```

### Key Principles

1. **No `login()` methods in EJBs** - Container handles authentication
2. **Use `@RolesAllowed`** - Declarative security at method level
3. **SessionContext for identity** - Get authenticated user via `getCallerPrincipal()`
4. **WildFly ApplicationRealm** - Stores users/roles in property files

---

## 📋 New Service Architecture

### Problem: Monolithic Service Design

Original design had one "BankService" doing everything. This violates separation of concerns.

### Solution: Decomposed Service Interfaces

We now have **6 focused service interfaces**:

| Service | Purpose | Roles |
|---------|---------|-------|
| `CustomerManagementService` | Create/search customers | employee |
| `StockSearchService` | Find stocks | employee, customer |
| `TradingService` | Buy/sell stocks | employee, customer |
| `PortfolioService` | View holdings | employee, customer |
| `BankVolumeService` | Check bank budget | employee |
| `AuthenticationCheckService` | Verify login | employee, customer |

### Benefits

- Clear responsibility boundaries
- Easier to apply role-based security
- Better testability
- Matches entity separation

---

## 🔐 Client Authentication Example

### How Clients Authenticate (RMI)

```java
// Client-side code
Properties props = new Properties();
props.put(Context.INITIAL_CONTEXT_FACTORY, 
          "org.wildfly.naming.client.WildFlyInitialContextFactory");
props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");

// Authentication happens HERE - not in business methods!
props.put(Context.SECURITY_PRINCIPAL, "alice");  // Username
props.put(Context.SECURITY_CREDENTIALS, "alice123");  // Password

Context ctx = new InitialContext(props);

// Lookup remote service
TradingService trading = (TradingService) ctx.lookup(
    "ejb:/ds-finance-bank-ejb/TradingServiceBean!at.ac.csw.dke.bank.service.TradingService"
);

// Call methods - container already authenticated!
BigDecimal price = trading.buyStock("MSFT", 10);
```

### What Happens

1. Client provides credentials in JNDI properties
2. WildFly validates against ApplicationRealm
3. Container creates authenticated context
4. All method calls have authenticated principal
5. EJBs get identity via `SessionContext.getCallerPrincipal()`

---

## 🔧 Fixed Service Interface Issues

### Problem 1: findStock Return Types

**Original Design:**
```java
StockDTO findStockBySymbol(String symbol);  // What if not found?
```

**Fixed:**
```java
Optional<StockDTO> findStockBySymbol(String symbol);  // Clear when not found
```

### Problem 2: Customer Identity in Parameters

**Original Design:**
```java
// ❌ WRONG - Customer could pass any ID!
BigDecimal buyStock(long customerId, String symbol, int shares);
```

**Fixed:**
```java
// ✅ CORRECT - No customerId parameter!
@RolesAllowed("customer")
BigDecimal buyStock(String symbol, int shares) {
    // Get customer from SessionContext
    String customerUsername = sessionContext.getCallerPrincipal().getName();
    // Customer can only trade for themselves!
}
```

### Problem 3: Pagination for Search Results

**Original Design:**
```java
List<StockDTO> findStocksByCompanyName(String query);  // Could return thousands!
```

**Fixed:**
```java
List<StockDTO> findStocksByCompanyName(String query, int maxResults);
```

---

## 📊 New DTO Structures

All DTOs are now properly designed as serializable data transfer objects:

- `PersonDTO` - For creating customers (includes login credentials)
- `CustomerDTO` - Customer information
- `StockDTO` - Stock information with current price
- `PortfolioDTO` - Complete portfolio with all positions
- `PortfolioPositionDTO` - Single stock position with gain/loss

### Example: PortfolioPositionDTO

```java
public class PortfolioPositionDTO implements Serializable {
    private String stockSymbol;
    private Integer quantity;
    private BigDecimal averagePurchasePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;  // Calculated
    private BigDecimal gainLoss;    // Calculated
    // ...
}
```

---

## 🎯 Implementation Checklist for Teil 2

As per instructor requirements, you must demonstrate:

### ✅ Required for Teil 2

- [x] Remote interfaces defined (completed in this commit)
- [x] EJB skeletons with security annotations (completed)
- [ ] **TODO:** One working client-server RMI call
- [ ] **TODO:** One TradingService SOAP call working
- [ ] **TODO:** One JPA entity persisted
- [ ] **TODO:** Container-managed security configured

### WildFly Configuration Required

1. **Create initial employee user:**
   ```bash
   ./add-user.sh
   # Type: Application User
   # Username: employee1
   # Password: (your choice)
   # Role: employee
   ```

2. **Configure datasource** (already done - see WILDFLY_DATASOURCE_SETUP.md)

3. **Test authentication:**
   ```java
   AuthenticationCheckService authCheck = ...;
   String result = authCheck.checkAuthentication();
   // Should return: "Authenticated as: employee1"
   ```

---

## 🚫 What NOT to Do

### Anti-Patterns to Avoid

```java
// ❌ Don't create stateful beans for authentication
@Stateful
public class LoginService {
    private String loggedInUser;
    public boolean login(String user, String pass) { ... }
}

// ❌ Don't pass passwords in business methods
public void createCustomer(String name, String password) { ... }

// ❌ Don't validate passwords in business logic
if (hashedPassword.equals(customer.getPassword())) { ... }

// ❌ Don't store authentication state in beans
private boolean isAuthenticated;
```

---

## 📚 Reference: Calculator Example

The instructor mentioned:
> "For the security part of Teil 2, you should review and follow how authentication is done in standard Java EE examples (e.g., the calculator example with identity store)."

### Key Lessons from Calculator Example

1. **No login() method** - Container handles it
2. **@RolesAllowed annotations** - Declarative security
3. **SessionContext usage** - Get authenticated principal
4. **JNDI properties for client auth** - Credentials passed at connection time

---

## 🔄 Migration Path

If you already have code with login() methods:

### Step 1: Remove Login Methods
```java
// DELETE these:
boolean login(String username, String password);
void logout();
```

### Step 2: Add Security Annotations
```java
@Stateless
@DeclareRoles({"employee", "customer"})
public class YourServiceBean {
    @Resource
    private SessionContext sessionContext;
    
    @RolesAllowed("employee")
    public void yourMethod() {
        String user = sessionContext.getCallerPrincipal().getName();
    }
}
```

### Step 3: Update Clients
```java
// Move authentication to JNDI properties
Properties props = new Properties();
props.put(Context.SECURITY_PRINCIPAL, username);
props.put(Context.SECURITY_CREDENTIALS, password);
Context ctx = new InitialContext(props);
```

---

## 📖 Summary of Changes

### Before (Wrong)
- ❌ Login methods in session beans
- ❌ Stateful beans for authentication
- ❌ Password validation in business logic
- ❌ Customer ID in method parameters
- ❌ Monolithic BankService

### After (Correct)
- ✅ Container-managed security
- ✅ Stateless session beans only
- ✅ WildFly ApplicationRealm for auth
- ✅ SessionContext for identity
- ✅ Decomposed service interfaces
- ✅ @RolesAllowed annotations
- ✅ Proper DTO structures
- ✅ Optional<> for nullable returns

---

## 🎓 Learning Resources

1. **Java EE Security Tutorial:** https://docs.oracle.com/javaee/7/tutorial/security-intro.htm
2. **WildFly Security:** https://docs.wildfly.org/28/Admin_Guide.html#Security
3. **EJB Security:** https://docs.oracle.com/javaee/7/tutorial/security-javaee003.htm

---

## ✅ Next Steps

1. Review this document with your team
2. Implement one service completely (suggest `AuthenticationCheckService`)
3. Test client-server RMI call
4. Add one TradingService SOAP call
5. Persist one entity (Customer or Stock)
6. Prepare Teil 2 presentation

---

**Author:** AI Assistant  
**Date:** January 21, 2026  
**Branch:** security-fixes-teil2  
**Status:** Ready for team review
