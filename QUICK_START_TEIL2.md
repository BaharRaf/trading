# Quick Start Guide - Teil 2 Implementation

## 🚀 What's New in This Branch

This branch (`security-fixes-teil2`) contains all the fixes for instructor feedback on Teil 2.

### ✅ What's Been Done

1. **✓ Remote Service Interfaces** (6 interfaces)
   - `CustomerManagementService` - Customer CRUD
   - `StockSearchService` - Stock search
   - `TradingService` - Buy/sell operations
   - `PortfolioService` - Portfolio viewing
   - `BankVolumeService` - Bank budget tracking
   - `AuthenticationCheckService` - Auth verification

2. **✓ DTOs** (7 data transfer objects)
   - `PersonDTO`, `CustomerDTO`, `StockDTO`
   - `PortfolioDTO`, `PortfolioPositionDTO`

3. **✓ Exception Classes**
   - `InsufficientFundsException`
   - `InsufficientSharesException`

4. **✓ EJB Implementations** (6 session beans)
   - All with `@Stateless` + `@RolesAllowed`
   - SessionContext injection
   - **NO login() methods!**

5. **✓ Comprehensive Documentation**
   - `TEIL2_SECURITY_FIXES.md` - Main explanation
   - `ARCHITECTURE_SECURITY_DESIGN.md` - Architecture diagrams

---

## 📝 Critical Changes from Original Design

| Original (Wrong) | Fixed (Correct) |
|------------------|----------------|
| `login()` methods in EJBs | Container-managed security |
| Stateful beans | Stateless beans only |
| Password in business logic | WildFly ApplicationRealm |
| Customer ID in parameters | SessionContext.getCallerPrincipal() |
| Monolithic BankService | 6 focused service interfaces |
| `StockDTO findStock()` | `Optional<StockDTO> findStock()` |

---

## 🛠️ Next Steps for Implementation

### Step 1: Review the Fixes
```bash
# Clone and checkout branch
git checkout security-fixes-teil2

# Read documentation
cat TEIL2_SECURITY_FIXES.md
cat ARCHITECTURE_SECURITY_DESIGN.md
```

### Step 2: Implement One Complete Service

Start with `AuthenticationCheckService` - it's the simplest:

```java
// Already done - just test it!
AuthenticationCheckServiceBean implements AuthenticationCheckService
```

### Step 3: Configure WildFly Security

```bash
# Add employee user
cd $WILDFLY_HOME/bin
./add-user.sh
# Type: Application User
# Username: employee1
# Password: employee123
# Role: employee
```

### Step 4: Test RMI Call (Teil 2 Requirement)

```java
// Client code
Properties props = new Properties();
props.put(Context.INITIAL_CONTEXT_FACTORY, 
          "org.wildfly.naming.client.WildFlyInitialContextFactory");
props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
props.put(Context.SECURITY_PRINCIPAL, "employee1");
props.put(Context.SECURITY_CREDENTIALS, "employee123");

Context ctx = new InitialContext(props);
AuthenticationCheckService auth = (AuthenticationCheckService) ctx.lookup(
    "ejb:/ds-finance-bank-ejb/AuthenticationCheckServiceBean!" +
    "at.ac.csw.dke.bank.service.AuthenticationCheckService"
);

System.out.println(auth.checkAuthentication());
// Should print: "Authenticated as: employee1"
```

### Step 5: Implement Trading Service SOAP Call (Teil 2 Requirement)

You already have the WSDL setup - just call one method:

```java
// In StockSearchServiceBean
public Optional<StockDTO> findStockBySymbol(String symbol) {
    // Call external TradingService
    TradingService tradingWS = ...; // From WSDL
    StockInfo info = tradingWS.getStockInfo(symbol);
    
    if (info == null) return Optional.empty();
    
    StockDTO dto = new StockDTO(null, symbol, info.getCompanyName(), 
                                info.getCurrentPrice());
    return Optional.of(dto);
}
```

### Step 6: Persist One Entity (Teil 2 Requirement)

```java
// In CustomerManagementServiceBean
public long createCustomer(PersonDTO person) {
    String employeeUsername = sessionContext.getCallerPrincipal().getName();
    
    // Create entity
    CustomerEntity customer = new CustomerEntity();
    customer.setFirstName(person.getFirstName());
    customer.setLastName(person.getLastName());
    customer.setAddress(person.getAddress());
    
    // Persist
    entityManager.persist(customer);
    entityManager.flush();
    
    // Generate customer number after ID is assigned
    customer.setCustomerNumber("CUST" + customer.getCustomerId());
    
    return customer.getCustomerId();
}
```

---

## 📚 Teil 2 Checklist

As per instructor requirements:

- [x] Design fixed for container-managed security
- [x] Remote interfaces defined
- [x] EJB implementations with @RolesAllowed
- [ ] **TODO:** Test one RMI call works
- [ ] **TODO:** Test one TradingService SOAP call works
- [ ] **TODO:** Test one entity persistence works
- [ ] **TODO:** Prepare oral report

---

## ⚠️ Common Mistakes to Avoid

### 1. Don't Create Login Methods
```java
// ❌ WRONG
public boolean login(String username, String password) { ... }

// ✅ RIGHT
// No login method at all!
// Authentication happens in JNDI context
```

### 2. Don't Pass Customer ID to Customer Methods
```java
// ❌ WRONG
public BigDecimal buyStock(long customerId, String symbol, int shares);

// ✅ RIGHT
public BigDecimal buyStock(String symbol, int shares) {
    String username = sessionContext.getCallerPrincipal().getName();
    // Lookup customer by username
}
```

### 3. Don't Validate Passwords in Business Logic
```java
// ❌ WRONG
if (hashedPassword.equals(storedPassword)) { ... }

// ✅ RIGHT
// Container already validated!
// Just use sessionContext.getCallerPrincipal()
```

---

## 💬 Teil 2 Presentation Outline

### 1. Oral Report (5 minutes)
- "We fixed authentication to use container-managed security"
- "No login() methods - WildFly ApplicationRealm handles auth"
- "6 focused service interfaces instead of monolithic BankService"
- "SessionContext gives us authenticated principal"

### 2. Demo: RMI Call (3 minutes)
- Show client authenticating with JNDI properties
- Call `checkAuthentication()` method
- Show result with username

### 3. Demo: TradingService SOAP (3 minutes)
- Show WSDL in project
- Call `getStockInfo()` or similar
- Display stock data

### 4. Demo: Entity Persistence (3 minutes)
- Create a CustomerEntity
- Persist to database
- Query back and display

---

## 🔗 File Locations

```
ds-finance-bank-common/src/main/java/at/ac/csw/dke/bank/
├── service/          # Remote interfaces
│   ├── CustomerManagementService.java
│   ├── TradingService.java
│   ├── PortfolioService.java
│   ├── StockSearchService.java
│   ├── BankVolumeService.java
│   └── AuthenticationCheckService.java
├── dto/              # Data transfer objects
│   ├── PersonDTO.java
│   ├── CustomerDTO.java
│   ├── StockDTO.java
│   ├── PortfolioDTO.java
│   └── PortfolioPositionDTO.java
└── exception/        # Exceptions
    ├── InsufficientFundsException.java
    └── InsufficientSharesException.java

ds-finance-bank-ejb/src/main/java/at/ac/csw/dke/bank/ejb/
├── CustomerManagementServiceBean.java
├── TradingServiceBean.java
├── PortfolioServiceBean.java
├── StockSearchServiceBean.java
├── BankVolumeServiceBean.java
└── AuthenticationCheckServiceBean.java
```

---

## 📧 Questions for Your Team

1. **Who will implement CustomerManagementService?**
2. **Who will handle TradingService integration?**
3. **Who will implement PortfolioService?**
4. **Who will set up the client authentication?**
5. **Who will prepare the Teil 2 presentation?**

---

## ℹ️ Support

If you have questions:

1. Read `TEIL2_SECURITY_FIXES.md` first
2. Check `ARCHITECTURE_SECURITY_DESIGN.md` for diagrams
3. Review the EJB skeleton implementations
4. Look at Java EE security tutorials

---

**Branch:** security-fixes-teil2  
**Status:** Ready for implementation  
**Next Deadline:** Teil 2 presentation
