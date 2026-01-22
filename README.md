# DS Finance Bank - Trading Service Integration

## Overview
This banking application integrates with the Trading Service API to allow employees and customers to search for stocks, buy shares, and sell shares through a real stock exchange web service.

## Trading Service API Integration

### What's Implemented
The `TradingServiceAdapterBean` provides the following functionality:
- **Stock Search**: `findStockQuotesByCompanyName(String companyName)` - Search for available stocks
- **Buy Stocks**: `buy(String symbol, int shares)` - Execute buy orders on the stock exchange
- **Sell Stocks**: `sell(String symbol, int shares)` - Execute sell orders on the stock exchange

### Technical Details
- **WSDL URL**: https://edu.dedisys.org/ds-finance/ws/TradingService?wsdl
- **SOAP Endpoint**: https://edu.dedisys.org/ds-finance/ws/TradingService
- **Generated Package**: `net.froihofer.dsfinance.ws.trading.api`
- **Authentication**: HTTP Basic Auth via system properties

## Setup Instructions

### 1. Configure WildFly System Properties

The Trading Service requires authentication credentials. Add these system properties to your WildFly configuration:

#### Option A: Edit standalone.conf (Recommended for permanent setup)

**Windows** (`WILDFLY_HOME/bin/standalone.conf.bat`):
```batch
set "JAVA_OPTS=%JAVA_OPTS% -Dtrading.ws.user=YOUR_USERNAME"
set "JAVA_OPTS=%JAVA_OPTS% -Dtrading.ws.pass=YOUR_PASSWORD"
```

**Linux/Mac** (`WILDFLY_HOME/bin/standalone.conf`):
```bash
JAVA_OPTS="$JAVA_OPTS -Dtrading.ws.user=YOUR_USERNAME"
JAVA_OPTS="$JAVA_OPTS -Dtrading.ws.pass=YOUR_PASSWORD"
```

#### Option B: IntelliJ Run Configuration

1. Open **Run → Edit Configurations**
2. Select your WildFly server configuration
3. In **Startup/Connection** tab, add to **VM options**:
```
-Dtrading.ws.user=YOUR_USERNAME -Dtrading.ws.pass=YOUR_PASSWORD
```

#### Option C: Command Line (Temporary)

```bash
./standalone.sh -Dtrading.ws.user=YOUR_USERNAME -Dtrading.ws.pass=YOUR_PASSWORD
```

**⚠️ IMPORTANT**: Replace `YOUR_USERNAME` and `YOUR_PASSWORD` with the credentials provided by your professor during the project briefing.

### 2. Configure Datasource

Add to `WILDFLY_HOME/standalone/configuration/standalone.xml` in the `<datasources>` section:

```xml
<datasource jndi-name="java:/datasources/DsFinanceBankDS"
    pool-name="DsFinanceBankDS" enabled="true" use-java-context="true">
  <connection-url>jdbc:h2:~/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>
  <driver>h2</driver>
  <security>
    <user-name>sa</user-name>
    <password>sa</password>
  </security>
</datasource>
```

### 3. Create WildFly Users

Create employee and customer users for authentication:

```bash
cd WILDFLY_HOME/bin

# Create an employee user
./add-user.sh -a -u employee1 -p password123 -g employee

# Create a customer user (do this AFTER creating customer via Employee client)
./add-user.sh -a -u customer1 -p secret123 -g customer
```

### 4. Build the Project

```bash
mvn clean package
```

The Maven build will:
- Generate Java classes from the Trading Service WSDL
- Compile all EJBs and web components
- Package everything into an EAR file

Generated classes will be in: `ds-finance-bank-ejb/target/generated-sources/cxf/`

### 5. Deploy to WildFly

Deploy the EAR file:
```
ds-finance-bank-ear/target/ds-finance-bank-ear-2.0-SNAPSHOT.ear
```

**Deployment Methods**:
- Copy to `WILDFLY_HOME/standalone/deployments/`
- Use WildFly Admin Console (http://localhost:9990)
- Deploy from your IDE (IntelliJ/NetBeans)

## Using the Trading Service

### Employee Client

1. **Launch** the employee client:
```bash
cd ds-finance-bank-client
java -jar target/ds-finance-bank-client-2.0-SNAPSHOT.jar
```

2. **Login** with employee credentials (e.g., `employee1/password123`)

3. **Create a Customer**:
   - Enter customer details (name, address)
   - Set initial investable volume (e.g., 100000)
   - Customer will be created in both the database and WildFly authentication

4. **Search for Stocks**:
   - Enter company name (e.g., "Apple", "Microsoft", "Tesla")
   - View available stocks with current prices

5. **Buy Stocks for Customer**:
   - Select customer
   - Enter stock symbol (e.g., "AAPL")
   - Enter number of shares
   - Buy order is executed on the stock exchange
   - Customer's depot is updated
   - Bank's investable volume is decreased

6. **Sell Stocks for Customer**:
   - Select customer
   - View customer's depot
   - Enter stock symbol and shares to sell
   - Sell order is executed on the stock exchange
   - Customer's depot is updated
   - Bank's investable volume is increased

7. **View Customer Depot**:
   - See all stock positions
   - Current value per position
   - Total depot value

8. **Check Bank Volume**:
   - View remaining investable volume at the stock exchange
   - Initial volume: 1,000,000,000 USD

### Customer Client

1. **Launch** the customer client:
```bash
cd ds-finance-bank-client
java -cp target/ds-finance-bank-client-2.0-SNAPSHOT.jar \
  net.froihofer.dsfinance.bank.client.gui.CustomerClientGUI
```

2. **Login** with customer credentials

3. **Search for Stocks**: Find stocks by company name

4. **Buy/Sell Stocks**: Manage your own portfolio

5. **View Depot**: Check your stock positions and total value

## How the Trading Service Works

### Stock Search Flow

1. User enters company name in the client
2. Client calls `EmployeeBankService.findStocks(companyName)`
3. EJB calls `TradingServiceAdapterBean.findStockQuotesByCompanyName()`
4. Adapter makes SOAP call to Trading Web Service
5. Results are cached in `StockEntity` table
6. Stock quotes returned to client

### Buy Order Flow

1. Employee/Customer initiates buy order
2. Client calls `buyStock(customerId, symbol, shares)`
3. `DepotServiceBean` validates customer has sufficient funds
4. `TradingServiceAdapterBean.buy(symbol, shares)` executes order at exchange
5. If successful:
   - Customer's depot position is created/updated
   - Bank's investable volume is decreased
   - Transaction is committed
6. If failed:
   - Transaction is rolled back
   - Error is returned to client

### Sell Order Flow

1. Employee/Customer initiates sell order
2. Client calls `sellStock(customerId, symbol, shares)`
3. `DepotServiceBean` validates customer has sufficient shares
4. `TradingServiceAdapterBean.sell(symbol, shares)` executes order at exchange
5. If successful:
   - Customer's depot position is decreased/removed
   - Bank's investable volume is increased
   - Transaction is committed
6. If failed:
   - Transaction is rolled back
   - Error is returned to client

## Project Structure

```
ds-finance-bank/
├── ds-finance-bank-ejb/           # EJB module (backend)
│   ├── src/main/java/
│   │   └── net/froihofer/dsfinance/bank/
│   │       ├── ejb/
│   │       │   ├── TradingServiceAdapterBean.java    ← Trading Service integration
│   │       │   ├── EmployeeBankServiceBean.java      ← Employee operations
│   │       │   ├── CustomerBankServiceBean.java      ← Customer operations
│   │       │   ├── DepotServiceBean.java             ← Depot management
│   │       │   └── CustomerServiceBean.java          ← Customer management
│   │       └── entity/
│   │           ├── BankEntity.java                   ← Bank volume tracking
│   │           ├── CustomerEntity.java
│   │           ├── DepotEntity.java
│   │           ├── DepotPositionEntity.java
│   │           └── StockEntity.java                  ← Stock cache
│   └── pom.xml                    ← CXF WSDL code generation configured here
├── ds-finance-bank-common/         # Shared interfaces and DTOs
├── ds-finance-bank-client/         # Swing GUI clients
├── ds-finance-bank-web/            # Web module (optional)
└── ds-finance-bank-ear/            # EAR packaging
```

## Troubleshooting

### Problem: "Missing required system property 'trading.ws.user'"

**Solution**: The Trading Service credentials are not configured. Add the system properties to WildFly as described in the setup section.

### Problem: "TradingService call failed" or Authentication Error

**Solutions**:
1. Verify your credentials are correct (check with professor)
2. Ensure system properties are set correctly in WildFly
3. Check WildFly console logs for detailed error messages
4. Restart WildFly after adding system properties

### Problem: WSDL code not generated

**Solution**:
```bash
mvn clean compile
```

The CXF plugin will fetch the WSDL and generate Java classes in `target/generated-sources/cxf/`

### Problem: "Insufficient funds" when buying

**Solution**: Check the bank's investable volume. If it's too low, sell some stocks to increase it, or create a new customer with lower initial volume.

### Problem: Connection timeout to Trading Service

**Solutions**:
1. Check your internet connection
2. Verify the Trading Service is accessible: https://edu.dedisys.org/ds-finance/ws/TradingService?wsdl
3. Check if your university network requires proxy settings

## Key Technical Features

- **Transaction Management**: All buy/sell operations use JTA transactions - if the Trading Service call fails, database changes are rolled back
- **Security**: Role-based access control with `@RolesAllowed({"employee", "customer"})`
- **Connection Pooling**: HTTP client configured with 5s connection timeout, 15s receive timeout
- **Stock Caching**: Stock symbols and company names are cached in the database for faster search
- **Error Handling**: Comprehensive exception handling with proper logging

## Technologies Used

- **Java EE 9+** (Jakarta EE)
- **EJB 4.0** for business logic
- **JPA 3.0** for persistence
- **JAX-WS** for SOAP web services
- **Apache CXF** for web service client
- **WildFly 28.0.1** application server
- **H2 Database** for data storage
- **Maven** for build management
- **Swing** for GUI clients

## Assignment Requirements Fulfilled

✅ Bank allows customers to search, buy, and sell stocks
✅ Bank manages depots for customers with stock positions
✅ Bank uses Trading Web Service for buy/sell operations
✅ Authentication required for web service calls
✅ Bank tracks investable volume (initial: 1 billion USD)
✅ Volume decreases on buy, increases on sell
✅ Remote interfaces for employee and customer clients
✅ Employee client: Create customers, search stocks, buy/sell for customers, view depots
✅ Customer client: Search stocks, buy/sell own stocks, view own depot
✅ Shared functionality implemented once with proper security
✅ JPA entities for persistent data
✅ Web service client code generated from WSDL

## Notes

- **Financial Data**: Stock prices from September 2024 onward are artificially generated and do not reflect real market prices
- **Usage Restriction**: This Trading Service and financial data may only be used for this course
- **Initial Bank Volume**: 1,000,000,000 USD per bank
- **WSDL Location**: The WSDL is fetched during Maven build from the online endpoint

## Authors

Project developed for the Distributed Systems course.

## License

This project is for educational purposes only.