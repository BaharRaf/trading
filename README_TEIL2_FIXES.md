# 🛠️ Teil 2 Fixes - Complete Guide

## 🎯 What's in This Branch

This branch (`security-fixes-teil2`) contains **ALL fixes** for:
1. ✅ Instructor feedback on authentication/security
2. ✅ Database persistence issue (data resets on restart)

---

## 🚀 Quick Start

### Fix 1: Database Persistence (5 minutes)

**Problem:** Data disappears when you restart WildFly

**Solution:** Run the automated script

```bash
# Linux/Mac
cd scripts
chmod +x fix-database-persistence.sh
./fix-database-persistence.sh

# Windows
cd scripts
fix-database-persistence.bat
```

**Manual alternative:** See `QUICK_FIX_DATABASE.md`

---

### Fix 2: Security Architecture (Review + Implement)

**Problem:** Login methods in session beans (instructor feedback)

**Solution:** New architecture with container-managed security

**Read these in order:**
1. `TEIL2_SECURITY_FIXES.md` - Main explanation
2. `QUICK_START_TEIL2.md` - Implementation guide
3. `ARCHITECTURE_SECURITY_DESIGN.md` - Diagrams

---

## 📁 File Structure

```
security-fixes-teil2/
├── Quick Guides
│   ├── README_TEIL2_FIXES.md        ⭐ START HERE
│   ├── QUICK_FIX_DATABASE.md         Database persistence (5 min)
│   └── QUICK_START_TEIL2.md          Security implementation
│
├── Detailed Documentation
│   ├── TEIL2_SECURITY_FIXES.md       Security architecture (detailed)
│   ├── ARCHITECTURE_SECURITY_DESIGN.md  Diagrams and flows
│   ├── DATABASE_PERSISTENCE_FIX.md   Database troubleshooting
│   └── CHANGELOG_TEIL2_FIXES.md      What changed
│
├── Scripts
│   ├── fix-database-persistence.sh   Linux/Mac script
│   └── fix-database-persistence.bat  Windows script
│
├── Config Files
│   └── datasource-persistent.xml     Ready-to-use datasource config
│
├── Remote Service Interfaces (6)
│   └── ds-finance-bank-common/src/main/java/at/ac/csw/dke/bank/service/
│       ├── CustomerManagementService.java
│       ├── TradingService.java
│       ├── PortfolioService.java
│       ├── StockSearchService.java
│       ├── BankVolumeService.java
│       └── AuthenticationCheckService.java
│
├── DTOs (7)
│   └── ds-finance-bank-common/src/main/java/at/ac/csw/dke/bank/dto/
│       ├── PersonDTO.java
│       ├── CustomerDTO.java
│       ├── StockDTO.java
│       ├── PortfolioDTO.java
│       └── PortfolioPositionDTO.java
│
├── Exceptions (2)
│   └── ds-finance-bank-common/src/main/java/at/ac/csw/dke/bank/exception/
│       ├── InsufficientFundsException.java
│       └── InsufficientSharesException.java
│
└── EJB Implementations (6)
    └── ds-finance-bank-ejb/src/main/java/at/ac/csw/dke/bank/ejb/
        ├── CustomerManagementServiceBean.java
        ├── TradingServiceBean.java
        ├── PortfolioServiceBean.java
        ├── StockSearchServiceBean.java
        ├── BankVolumeServiceBean.java
        └── AuthenticationCheckServiceBean.java
```

---

## ✅ Checklist: Apply All Fixes

### Step 1: Database Persistence
- [ ] Run `scripts/fix-database-persistence.sh` (or .bat)
- [ ] Restart WildFly
- [ ] Create a test customer
- [ ] Restart WildFly again
- [ ] Verify customer still exists
- [ ] Check database file exists:
  ```bash
  ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db
  ```

### Step 2: Review Security Architecture
- [ ] Read `TEIL2_SECURITY_FIXES.md`
- [ ] Understand why login() methods are wrong
- [ ] Understand container-managed security
- [ ] Review the 6 new service interfaces
- [ ] Review the EJB implementations with @RolesAllowed

### Step 3: Configure WildFly Security
- [ ] Add initial employee user:
  ```bash
  $WILDFLY_HOME/bin/add-user.sh
  # Type: Application User
  # Username: employee1
  # Password: (your choice)
  # Role: employee
  ```

### Step 4: Implement Teil 2 Requirements
- [ ] One RMI call working (use `AuthenticationCheckService`)
- [ ] One SOAP call working (implement in `StockSearchServiceBean`)
- [ ] One entity persisted (implement in `CustomerManagementServiceBean`)

### Step 5: Test Everything
- [ ] Employee can authenticate
- [ ] Customer can authenticate (create via employee first)
- [ ] Employee can access employee-only methods
- [ ] Customer CANNOT access employee methods
- [ ] Data persists across restarts

---

## 📊 What Changed

### Before (Problems)
❌ Login methods in session beans  
❌ Stateful beans for authentication  
❌ Password validation in business logic  
❌ Database resets on restart  
❌ Monolithic service interface  

### After (Fixed)
✅ Container-managed security  
✅ All beans are @Stateless  
✅ WildFly ApplicationRealm handles auth  
✅ Database persists correctly  
✅ 6 focused service interfaces  
✅ Proper DTOs and exceptions  
✅ @RolesAllowed on all methods  

---

## 📚 Documentation Index

### For Quick Fixes
| Document | Purpose | Time |
|----------|---------|------|
| `QUICK_FIX_DATABASE.md` | Fix database persistence | 5 min |
| `QUICK_START_TEIL2.md` | Implement security fixes | 15 min |

### For Understanding
| Document | Purpose | Time |
|----------|---------|------|
| `TEIL2_SECURITY_FIXES.md` | Why we changed everything | 30 min |
| `ARCHITECTURE_SECURITY_DESIGN.md` | How it all works | 20 min |

### For Troubleshooting
| Document | Purpose |
|----------|----------|
| `DATABASE_PERSISTENCE_FIX.md` | Database issues deep dive |
| `CHANGELOG_TEIL2_FIXES.md` | Complete list of changes |

---

## ⚡ Quick Commands

### Fix Database (Choose one)

```bash
# Linux/Mac - Automated
./scripts/fix-database-persistence.sh

# Windows - Automated
scripts\fix-database-persistence.bat

# Manual - All platforms
# Edit: $WILDFLY_HOME/standalone/configuration/standalone.xml
# Change: jdbc:h2:~/ds_finance_bank
# To: jdbc:h2:${jboss.server.data.dir}/ds_finance_bank;DB_CLOSE_DELAY=-1
```

### Add WildFly User

```bash
# Linux/Mac
$WILDFLY_HOME/bin/add-user.sh

# Windows
%WILDFLY_HOME%\bin\add-user.bat

# Then:
# - Type: Application User
# - Username: employee1 (or customer1)
# - Password: (your choice)
# - Role: employee (or customer)
```

### Check Database File

```bash
# Linux/Mac
ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db

# Windows
dir %WILDFLY_HOME%\standalone\data\ds_finance_bank.mv.db
```

### View Logs

```bash
# Linux/Mac
tail -f $WILDFLY_HOME/standalone/log/server.log

# Windows
type %WILDFLY_HOME%\standalone\log\server.log
```

---

## 🎯 Teil 2 Presentation Checklist

### What to Show
1. ✅ **Explain authentication fix**
   - "We moved authentication to WildFly container"
   - "No more login() methods in business code"
   - Show `@RolesAllowed` annotations

2. ✅ **Demo RMI call**
   - Show client authenticating via JNDI
   - Call `checkAuthentication()` method
   - Display authenticated username

3. ✅ **Demo SOAP call**
   - Call external TradingService
   - Show stock data retrieval

4. ✅ **Demo entity persistence**
   - Create customer via Employee GUI
   - Show in database
   - **Restart WildFly**
   - Show customer still exists!

5. ✅ **Explain changes from original design**
   - Point out security architecture diagram
   - Explain service decomposition

---

## 👥 Team Collaboration

### Suggested Task Division

**Person 1:** Database fix + testing
- Run persistence script
- Verify data survives restarts
- Document any issues

**Person 2:** Authentication setup
- Create WildFly users (employee + customer)
- Test container-managed security
- Implement `AuthenticationCheckService`

**Person 3:** Trading service integration
- Implement `StockSearchServiceBean`
- Call external SOAP service
- Test stock search

**Person 4:** Customer management
- Implement `CustomerManagementServiceBean`
- Test customer creation
- Verify persistence

**Person 5:** Presentation
- Prepare slides
- Demo script
- Explanation of changes

---

## ❓ FAQ

**Q: Will this break our existing code?**  
A: No - this is in a separate branch. You can merge what you need.

**Q: Do we have to implement everything now?**  
A: For Teil 2, you only need:
- One RMI call working
- One SOAP call working
- One entity persisted
- Understanding of the security changes

**Q: What if the database fix doesn't work?**  
A: See `DATABASE_PERSISTENCE_FIX.md` for detailed troubleshooting.

**Q: Can we still use our old GUI?**  
A: Yes, just update authentication to use JNDI properties instead of login() methods.

**Q: Where do we put our existing business logic?**  
A: Fill in the `TODO` sections in the EJB beans with your code.

---

## 📞 Support

### If Database Still Resets
1. Check `DATABASE_PERSISTENCE_FIX.md`
2. Verify datasource URL in standalone.xml
3. Check file exists: `$WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db`
4. Check WildFly logs for errors

### If Authentication Doesn't Work
1. Check user exists:
   ```bash
   cat $WILDFLY_HOME/standalone/configuration/application-users.properties
   cat $WILDFLY_HOME/standalone/configuration/application-roles.properties
   ```
2. Verify JNDI properties in client code
3. Check EJB has `@RolesAllowed` annotation

### If Confused About Security
1. Start with `TEIL2_SECURITY_FIXES.md`
2. Look at `AuthenticationCheckServiceBean` - simplest example
3. Review calculator example from course materials

---

## ✅ Success Criteria

You'll know everything works when:

1. ✅ Create customer → restart WildFly → customer still exists
2. ✅ Employee can login and access employee methods
3. ✅ Customer can login and access customer methods
4. ✅ Customer CANNOT access employee methods
5. ✅ RMI call works with authentication
6. ✅ SOAP call to TradingService works
7. ✅ No `login()` methods in your EJBs
8. ✅ `SessionContext` used for identity

---

## 🎓 Learning Outcomes

After implementing these fixes, you'll understand:
- Container-managed security in Java EE
- Why stateless beans are better than stateful
- H2 database file persistence
- Role-based access control with `@RolesAllowed`
- Service decomposition and separation of concerns
- WildFly ApplicationRealm
- RMI with authentication
- Proper DTO usage

---

**Branch:** security-fixes-teil2  
**Status:** ✅ Ready to use  
**Last Updated:** January 21, 2026  

**Next Steps:** Fix database → Review security docs → Implement Teil 2 requirements → Test everything → Prepare presentation
