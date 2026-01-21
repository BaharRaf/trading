# WildFly H2 Datasource Configuration

## Problem
Deployment fails with error:
```
WFLYCTL0412: Required services that are not installed: jboss.jdbc-driver.h2
```

This means the H2 database datasource is not configured in WildFly.

---

## Solution: Configure H2 Datasource

You have **two options**:

### **Option 1: Use WildFly CLI (Recommended)**

1. **Start WildFly** (if not already running):
   ```bash
   cd C:\Programs\wildfly-28.0.1.Final-dev\bin
   standalone.bat
   ```

2. **Open another terminal** and run WildFly CLI:
   ```bash
   cd C:\Programs\wildfly-28.0.1.Final-dev\bin
   jboss-cli.bat --connect
   ```

3. **Add H2 datasource** (copy/paste these commands):
   ```bash
   # Add H2 datasource
   data-source add \
     --name=DsFinanceBankDS \
     --jndi-name=java:/datasources/DsFinanceBankDS \
     --driver-name=h2 \
     --connection-url=jdbc:h2:mem:dsfinancebank;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE \
     --user-name=sa \
     --password=sa \
     --use-java-context=true \
     --enabled=true

   # Verify it was created
   /subsystem=datasources/data-source=DsFinanceBankDS:read-resource
   ```

4. **Exit CLI**:
   ```bash
   exit
   ```

5. **Redeploy your application**:
   ```bash
   mvn clean install
   ```

---

### **Option 2: Manual Edit of standalone.xml**

1. **Stop WildFly** (Ctrl+C in the server terminal)

2. **Edit configuration file**:
   ```
   C:\Programs\wildfly-28.0.1.Final-dev\standalone\configuration\standalone.xml
   ```

3. **Find the `<datasources>` section** (around line 140-180):
   ```xml
   <subsystem xmlns="urn:jboss:domain:datasources:7.1">
       <datasources>
           <datasource jndi-name="java:jboss/datasources/ExampleDS" pool-name="ExampleDS" enabled="true" use-java-context="true" statistics-enabled="${wildfly.datasources.statistics-enabled:${wildfly.statistics-enabled:false}}">
               ...
           </datasource>
   ```

4. **Add this datasource definition** right after the ExampleDS datasource:
   ```xml
   <datasource jndi-name="java:/datasources/DsFinanceBankDS" pool-name="DsFinanceBankDS" enabled="true" use-java-context="true">
       <connection-url>jdbc:h2:mem:dsfinancebank;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>
       <driver>h2</driver>
       <security>
           <user-name>sa</user-name>
           <password>sa</password>
       </security>
       <validation>
           <validate-on-match>false</validate-on-match>
           <background-validation>false</background-validation>
       </validation>
       <statement>
           <share-prepared-statements>false</share-prepared-statements>
       </statement>
   </datasource>
   ```

5. **Verify H2 driver is listed** (should already exist in same `<datasources>` section):
   ```xml
   <drivers>
       <driver name="h2" module="com.h2database.h2">
           <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
       </driver>
   </drivers>
   ```

6. **Save the file** and **restart WildFly**:
   ```bash
   cd C:\Programs\wildfly-28.0.1.Final-dev\bin
   standalone.bat
   ```

7. **Redeploy**:
   ```bash
   mvn clean install
   ```

---

## Verification

### 1. Check Datasource in WildFly CLI
```bash
jboss-cli.bat --connect
/subsystem=datasources/data-source=DsFinanceBankDS:test-connection-in-pool
```

Should return:
```
{
    "outcome" => "success",
    "result" => [true]
}
```

### 2. Check Server Log
Look for this line in `wildfly/standalone/log/server.log`:
```
Bound data source [java:/datasources/DsFinanceBankDS]
```

### 3. Check Deployment
After `mvn clean install`, you should see:
```
ds-finance-bank-ear-2.0-SNAPSHOT.ear.deployed
```

No error messages about missing services.

---

## Database Configuration Details

### In-Memory H2 Database
- **Type:** In-memory (data lost on restart)
- **URL:** `jdbc:h2:mem:dsfinancebank`
- **User:** `sa`
- **Password:** `sa`
- **Dialect:** H2Dialect

### Persistence Settings
- **Auto-DDL:** `update` (automatically creates/updates tables)
- **Transaction Type:** JTA (Java Transaction API)
- **Provider:** Hibernate

### To Switch to File-Based Database

If you want data to persist between restarts, change the connection URL:

**In CLI:**
```bash
/subsystem=datasources/data-source=DsFinanceBankDS:write-attribute(name=connection-url, value="jdbc:h2:file:C:/data/dsfinancebank;AUTO_SERVER=TRUE")
```

**In standalone.xml:**
```xml
<connection-url>jdbc:h2:file:C:/data/dsfinancebank;AUTO_SERVER=TRUE</connection-url>
```

---

## Troubleshooting

### Error: "H2 driver not found"
**Solution:** H2 is included with WildFly by default. Check:
```bash
dir C:\Programs\wildfly-28.0.1.Final-dev\modules\system\layers\base\com\h2database\h2
```

You should see `h2-*.jar` file.

### Error: "Datasource already exists"
**Solution:** Remove existing datasource first:
```bash
jboss-cli.bat --connect
data-source remove --name=DsFinanceBankDS
```

Then add it again.

### Error: "Cannot connect to CLI"
**Solution:** Make sure WildFly is running first:
```bash
# Terminal 1
standalone.bat

# Terminal 2 (wait 30 seconds after starting WildFly)
jboss-cli.bat --connect
```

### Deployment Still Fails
1. **Check WildFly logs:**
   ```
   wildfly\standalone\log\server.log
   ```

2. **Verify datasource is bound:**
   ```bash
   jboss-cli.bat --connect
   /subsystem=datasources:read-resource
   ```

3. **Clear deployments directory:**
   ```bash
   del C:\Programs\wildfly-28.0.1.Final-dev\standalone\deployments\*.ear
   del C:\Programs\wildfly-28.0.1.Final-dev\standalone\deployments\*.failed
   ```

4. **Rebuild and redeploy:**
   ```bash
   mvn clean install
   ```

---

## Quick Fix Summary

**If you just want it to work quickly:**

```bash
# 1. Start WildFly
cd C:\Programs\wildfly-28.0.1.Final-dev\bin
start standalone.bat

# 2. Wait 30 seconds, then configure datasource
jboss-cli.bat --connect --command="data-source add --name=DsFinanceBankDS --jndi-name=java:/datasources/DsFinanceBankDS --driver-name=h2 --connection-url=jdbc:h2:mem:dsfinancebank;DB_CLOSE_DELAY=-1 --user-name=sa --password=sa --enabled=true"

# 3. Test connection
jboss-cli.bat --connect --command="/subsystem=datasources/data-source=DsFinanceBankDS:test-connection-in-pool"

# 4. Redeploy
cd your-project-directory
mvn clean install
```

**Done!** Your application should now deploy successfully.

---

## Additional Resources

- [WildFly Datasource Configuration](https://docs.wildfly.org/28/Admin_Guide.html#DataSource)
- [H2 Database Documentation](https://www.h2database.com/)
- [JBoss CLI Guide](https://docs.wildfly.org/28/Admin_Guide.html#Command_Line_Interface)

---

**Created:** January 21, 2026  
**Repository:** [BaharRaf/trading](https://github.com/BaharRaf/trading)  