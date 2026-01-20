# DS Finance Bank - Trading System

A Java EE enterprise banking application with stock trading capabilities, built with WildFly, JPA, and Swing GUI.

## Project Structure

```
ds-finance-bank/
├── ds-finance-bank-common/     # Shared interfaces and DTOs
├── ds-finance-bank-ejb/        # Business logic (EJB services)
├── ds-finance-bank-web/        # Web services and REST endpoints
├── ds-finance-bank-ear/        # Enterprise archive packaging
└── ds-finance-bank-client/     # Swing GUI client application
```

## Technology Stack

- **Java:** 17
- **Application Server:** WildFly 28.0.1.Final
- **Framework:** Jakarta EE 10.0.0
- **Persistence:** JPA 3.0 / Hibernate
- **Client:** Swing GUI
- **Build Tool:** Maven
- **Security:** WildFly ApplicationRealm

## Features

### Employee Interface
- Create and manage customer accounts
- Search customers by name
- Buy/sell stocks on behalf of customers
- View customer portfolios
- Monitor bank's investable volume

### Customer Interface
- View personal portfolio
- Check stock positions
- See total portfolio value
- Track average purchase prices

### Backend Services
- EJB-based business logic
- JPA entity management
- Integration with external trading service
- Automatic WildFly user provisioning
- Transaction management

## Prerequisites

1. **Java Development Kit (JDK) 17+**
   ```bash
   java -version  # Should show 17 or higher
   ```

2. **Apache Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **WildFly 28.0.1.Final**
   - Download from: https://www.wildfly.org/downloads/
   - Extract to a directory (e.g., `C:\Programs\wildfly-28.0.1.Final-dev`)

4. **Database** (H2 embedded - included with WildFly)

## Setup Instructions

### 1. Configure WildFly

#### Start WildFly Server
```bash
cd <WILDFLY_HOME>/bin

# Windows
standalone.bat

# Linux/Mac
./standalone.sh
```

#### Add Initial Employee User
```bash
# In another terminal, run:
cd <WILDFLY_HOME>/bin

# Windows
add-user.bat

# Linux/Mac  
./add-user.sh
```

**Configure as:**
- Type: `Application User`
- Username: `customer`
- Password: `customerpass`
- Realm: `ApplicationRealm`
- Role: `employee`

### 2. Configure Project

Edit `pom.xml` in the root directory:

```xml
<wildfly.deploy.dir>YOUR_WILDFLY_PATH/standalone/deployments</wildfly.deploy.dir>
```

Replace with your actual WildFly path, e.g.:
- Windows: `C:\Programs\wildfly-28.0.1.Final-dev\standalone\deployments`
- Linux: `/opt/wildfly/standalone/deployments`

### 3. Build and Deploy

```bash
# Build the entire project
mvn clean install

# The EAR file will be automatically deployed to WildFly
# Check: <WILDFLY_HOME>/standalone/deployments/ds-finance-bank-ear-2.0-SNAPSHOT.ear.deployed
```

### 4. Run the Client Application

```bash
cd ds-finance-bank-client/target

java -jar ds-finance-bank-client-2.0-SNAPSHOT-jar-with-dependencies.jar
```

## Usage Guide

### Option 1: Employee GUI

1. Select "Employee GUI" from launcher
2. Login with:
   - Username: `customer`
   - Password: `customerpass`
3. **Create a Customer:**
   - Fill in first name, last name, address
   - **Important:** Set Login Username and Password for WildFly authentication
   - Click "Create Customer"
   - Customer is now in both database AND WildFly
4. **Manage Stocks:**
   - Search for stocks by company name
   - Buy/sell stocks for customers
   - View customer portfolios

### Option 2: Customer GUI

1. Select "Customer GUI" from launcher
2. Login with credentials created by employee (e.g., `alice` / `alice123`)
3. View your portfolio:
   - Current stock positions
   - Purchase prices
   - Current market values
   - Total portfolio value

### Option 3: CLI Demo (Part 2)

1. Select "Part 2 CLI Demo"
2. Automated demo of all features
3. Watch console output for results

## Architecture Details

### Authentication Flow

```
[Client GUI] --> [WildFly Security] --> [EJB Service] --> [Database]
                       |
                       v
            ApplicationRealm
            (application-users.properties)
```

### Customer Creation Workflow

1. Employee enters customer details in GUI
2. `CustomerManagementPanel` generates unique `customerNumber`
3. EJB `EmployeeBankServiceBean.createCustomer()` is called
4. Customer entity persisted to database
5. `WildflyAuthDBHelper` adds user to WildFly:
   - Hash password using MD5 (WildFly format)
   - Update `application-users.properties`
   - Update `application-roles.properties` with "customer" role
6. Customer can immediately log in to Customer GUI

### Stock Trading Workflow

1. Employee searches for stock by company name
2. `TradingServiceAdapterBean` queries external trading service
3. Employee buys/sells stocks for customer
4. `DepotEntity` and `DepotPositionEntity` updated
5. Bank's available volume adjusted
6. Average purchase price calculated

## Database Schema

### Main Entities

- **CustomerEntity:** Customer account information
- **DepotEntity:** Customer's stock depot (1-to-1 with Customer)
- **DepotPositionEntity:** Individual stock positions
- **StockEntity:** Stock information (symbol, company name)
- **BankEntity:** Bank's investable volume management

### Relationships

```
Customer (1) -----> (1) Depot
                     |
                     v
                    (N) DepotPosition
                     |
                     v
                    (1) Stock
```

## Troubleshooting

### Issue: "Customer created in DB but WildFly user creation failed"

**Cause:** WildFly properties files not writable or jboss.home.dir not set

**Solution:**
```bash
# Check WildFly is running
jps | grep jboss  # Should show WildFly process

# Verify system property
echo $JBOSS_HOME  # Should point to WildFly directory

# Check file permissions
ls -l $WILDFLY_HOME/standalone/configuration/application-*.properties
```

### Issue: "Cannot connect to EJB"

**Cause:** WildFly not running or deployment failed

**Solution:**
```bash
# Check WildFly logs
tail -f <WILDFLY_HOME>/standalone/log/server.log

# Verify deployment
ls <WILDFLY_HOME>/standalone/deployments/*.deployed

# Redeploy if needed
mvn clean install -DskipTests
```

### Issue: "Login fails for newly created customer"

**Cause:** WildFly needs to reload authentication realm

**Solution:**
1. Wait 30 seconds for WildFly to detect property file changes
2. OR restart WildFly server
3. Verify user in `application-users.properties`

## Development

### Run Tests
```bash
mvn test
```

### Debug Mode
```bash
# Start WildFly in debug mode
<WILDFLY_HOME>/bin/standalone.sh --debug

# Connect debugger to port 8787
```

### Hot Reload
```bash
# After code changes
mvn clean install -DskipTests

# WildFly auto-redeploys
```

## Recent Fixes

See [BUGFIX_WILDFLY_AUTH.md](BUGFIX_WILDFLY_AUTH.md) for details on the customer authentication fix (January 20, 2026).

## Contributors

- **Bahareh Rafiee** (BaharRaf)
- **Sepidehri**
- **Discliper** (MP)

## License

Academic project - FH Campus Wien

## Support

For issues, please check:
1. WildFly server logs
2. Console output from client application
3. Database state via H2 console
4. WildFly authentication files

---

**Last Updated:** January 20, 2026  
**Version:** 2.0-SNAPSHOT  
**Server:** WildFly 28.0.1.Final  