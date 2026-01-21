# Architecture & Security Design - Container-Managed Security

## Authentication Flow Diagram

```
┌─────────────────┐
│                 │
│  Client (GUI)   │
│                 │
└────────┬────────┘
         │
         │ 1. Create InitialContext with
         │    - SECURITY_PRINCIPAL (username)
         │    - SECURITY_CREDENTIALS (password)
         │
         ▼
┌─────────────────────────────────────────┐
│                                         │
│         WildFly Security Layer          │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │   ApplicationRealm                │ │
│  │   ┌──────────────────────────┐    │ │
│  │   │ application-users.properties │ │
│  │   │ employee1=<hash>         │    │ │
│  │   │ alice=<hash>             │    │ │
│  │   └──────────────────────────┘    │ │
│  │   ┌──────────────────────────┐    │ │
│  │   │ application-roles.properties │ │
│  │   │ employee1=employee       │    │ │
│  │   │ alice=customer           │    │ │
│  │   └──────────────────────────┘    │ │
│  └───────────────────────────────────┘ │
│                                         │
└────────┬────────────────────────────────┘
         │
         │ 2. Container validates credentials
         │    and creates authenticated context
         │
         ▼
┌─────────────────────────────────────────┐
│                                         │
│          EJB Container                  │
│                                         │
│  3. Before method invocation:           │
│     - Check @RolesAllowed annotation   │
│     - Verify caller has required role  │
│     - Inject SessionContext            │
│                                         │
└────────┬────────────────────────────────┘
         │
         │ 4. If authorized, invoke method
         │
         ▼
┌─────────────────────────────────────────┐
│                                         │
│    @Stateless Session Bean             │
│                                         │
│    @RolesAllowed("employee")            │
│    public void someMethod() {           │
│        String user =                    │
│          sessionContext                 │
│            .getCallerPrincipal()        │
│            .getName();                  │
│        // Business logic here           │
│    }                                    │
│                                         │
└─────────────────────────────────────────┘
```

---

## Service Layer Architecture

```
┌───────────────────────────────────────────────────────────────┐
│                      CLIENT TIER                              │
│  ┌──────────────────┐        ┌──────────────────┐            │
│  │  Employee Client │        │  Customer Client │            │
│  │   (Swing GUI)    │        │   (Swing GUI)    │            │
│  └────────┬─────────┘        └────────┬─────────┘            │
└───────────┼──────────────────────────┼────────────────────────┘
            │                           │
            │ RMI with credentials      │ RMI with credentials
            │                           │
┌───────────┼──────────────────────────┼────────────────────────┐
│           │    WildFly Container     │                        │
│           │   (Security + EJB)       │                        │
│           ▼                           ▼                        │
│  ┌─────────────────────────────────────────────────────┐     │
│  │          REMOTE EJB INTERFACES (Common Module)      │     │
│  ├─────────────────────────────────────────────────────┤     │
│  │ • CustomerManagementService    (employee)           │     │
│  │ • StockSearchService           (employee, customer) │     │
│  │ • TradingService               (employee, customer) │     │
│  │ • PortfolioService             (employee, customer) │     │
│  │ • BankVolumeService            (employee)           │     │
│  │ • AuthenticationCheckService   (employee, customer) │     │
│  └────────────────────┬────────────────────────────────┘     │
│                       │                                       │
│                       │ Implemented by                        │
│                       ▼                                       │
│  ┌─────────────────────────────────────────────────────┐     │
│  │         STATELESS SESSION BEANS (EJB Module)        │     │
│  ├─────────────────────────────────────────────────────┤     │
│  │ @Stateless + @RolesAllowed                          │     │
│  │ ┌─────────────────────────────────────────────┐     │     │
│  │ │ CustomerManagementServiceBean               │     │     │
│  │ │   @Resource SessionContext sessionContext   │     │     │
│  │ │   @RolesAllowed("employee")                 │     │     │
│  │ └─────────────────────────────────────────────┘     │     │
│  │ • TradingServiceBean                                │     │
│  │ • PortfolioServiceBean                              │     │
│  │ • ... (all other service beans)                     │     │
│  └────────────────────┬────────────────────────────────┘     │
│                       │                                       │
│                       │ Uses                                  │
│                       ▼                                       │
│  ┌─────────────────────────────────────────────────────┐     │
│  │      LOCAL EJBs / JPA Repositories                  │     │
│  ├─────────────────────────────────────────────────────┤     │
│  │ • CustomerRepository (JPA operations)               │     │
│  │ • DepotRepository (JPA operations)                  │     │
│  │ • StockRepository (JPA operations)                  │     │
│  │ • TradingServiceAdapter (SOAP client)               │     │
│  │ • WildflyAuthDBHelper (user management)             │     │
│  └────────────────────┬────────────────────────────────┘     │
│                       │                                       │
│                       │ Persists to                           │
│                       ▼                                       │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              JPA ENTITIES                           │     │
│  ├─────────────────────────────────────────────────────┤     │
│  │ • CustomerEntity                                    │     │
│  │ • DepotEntity                                       │     │
│  │ • DepotPositionEntity                               │     │
│  │ • StockEntity                                       │     │
│  │ • TransactionEntity                                 │     │
│  │ • BankEntity                                        │     │
│  └────────────────────┬────────────────────────────────┘     │
└────────────────────────┼──────────────────────────────────────┘
                         │
                         ▼
              ┌────────────────────┐
              │  H2 Database       │
              │  (JPA/Hibernate)   │
              └────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  EXTERNAL SERVICES                          │
│  ┌──────────────────────────────────────────────────┐       │
│  │   TradingService (SOAP Web Service)              │       │
│  │   https://edu.dedisys.org/ds-finance/ws/...      │       │
│  └──────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

---

## Security Matrix

| Service Method | Employee Role | Customer Role | Authentication |
|----------------|---------------|---------------|----------------|
| `createCustomer()` | ✅ Allowed | ❌ Denied | Container |
| `findCustomer(id)` | ✅ Allowed | ❌ Denied | Container |
| `findStocksByName()` | ✅ Allowed | ✅ Allowed | Container |
| `buyStockForCustomer(id, ...)` | ✅ Allowed | ❌ Denied | Container |
| `buyStock(...)` | ❌ Denied | ✅ Allowed | Container |
| `getCustomerPortfolio(id)` | ✅ Allowed | ❌ Denied | Container |
| `getMyPortfolio()` | ❌ Denied | ✅ Allowed | Container |
| `getAvailableTradingBudget()` | ✅ Allowed | ❌ Denied | Container |
| `checkAuthentication()` | ✅ Allowed | ✅ Allowed | Container |

---

## Method Invocation Flow Example

### Scenario: Customer Buys Stock

```
1. Customer GUI calls:
   tradingService.buyStock("MSFT", 10)

2. WildFly intercepts call:
   - Checks credentials from JNDI context
   - Validates against ApplicationRealm
   - Creates authenticated principal: "alice"

3. EJB Container checks security:
   - Method has @RolesAllowed("customer")
   - Principal "alice" has role "customer" ✅
   - Injects SessionContext into bean

4. TradingServiceBean.buyStock() executes:
   String customerUsername = sessionContext.getCallerPrincipal().getName();
   // customerUsername = "alice"
   
   - Lookup CustomerEntity by username
   - Check bank has enough volume
   - Call external TradingService SOAP
   - Update DepotPositionEntity
   - Update BankEntity.availableVolume
   - Create TransactionEntity
   - Return purchase price

5. Result returned to client
```

---

## Key Design Decisions

### 1. No Login Methods
**Rationale:** Container handles authentication, not business logic

### 2. Separate Interfaces for Employee vs Customer
**Rationale:** 
- Clear security boundaries
- Customers can't access employee functions
- Employee can help any customer, customer can only access own data

### 3. SessionContext for Identity
**Rationale:**
- Standard Java EE approach
- Container-managed, no manual validation
- Audit trail (who did what)

### 4. Stateless Beans Only
**Rationale:**
- Better scalability
- No session state to manage
- Container can pool instances

### 5. DTOs for Data Transfer
**Rationale:**
- Entities stay server-side
- Clear API contracts
- Serializable for RMI

---

## Testing Strategy

### Unit Tests (JUnit)
```java
@Test
public void testAuthenticationCheck() {
    // Mock SessionContext
    when(sessionContext.getCallerPrincipal().getName())
        .thenReturn("testuser");
    
    String result = authService.checkAuthentication();
    assertEquals("Authenticated as: testuser", result);
}
```

### Integration Tests
```java
@Test
public void testCustomerCannotAccessEmployeeMethod() {
    // Login as customer
    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL, "alice");
    props.put(Context.SECURITY_CREDENTIALS, "alice123");
    
    // Try to call employee-only method
    assertThrows(EJBAccessException.class, () -> {
        customerMgmt.createCustomer(new PersonDTO(...));
    });
}
```

---

**Last Updated:** January 21, 2026
