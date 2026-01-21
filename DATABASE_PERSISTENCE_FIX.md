# Fix: Database Resets on WildFly Restart

## Problem

When you restart WildFly, all data is lost (customers, depots, etc.) and you have to create everything again.

## Root Cause Analysis

Your `persistence.xml` is correctly configured with `hibernate.hbm2ddl.auto=update`, so the issue is likely with the **H2 database file persistence**.

---

## ✅ Solution 1: Fix H2 Database File Path (RECOMMENDED)

### Problem
The current datasource uses:
```xml
jdbc:h2:~/ds_finance_bank;AUTO_SERVER=TRUE
```

The `~` (tilde) might not expand correctly in all environments, causing H2 to use in-memory or temporary storage.

### Fix

**Option A: Use Absolute Path (Most Reliable)**

1. **Stop WildFly**

2. **Edit datasource configuration:**
   - File: `$WILDFLY_HOME/standalone/configuration/standalone.xml`
   - Find the DsFinanceBankDS datasource
   - Change the connection URL:

```xml
<!-- BEFORE -->
<connection-url>jdbc:h2:~/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>

<!-- AFTER (Windows example) -->
<connection-url>jdbc:h2:C:/data/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>

<!-- AFTER (Linux/Mac example) -->
<connection-url>jdbc:h2:/home/youruser/data/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>
```

3. **Create the directory:**
```bash
# Windows
mkdir C:\data

# Linux/Mac
mkdir -p /home/youruser/data
```

4. **Restart WildFly**

5. **Verify database file is created:**
```bash
# Windows
dir C:\data\ds_finance_bank.mv.db

# Linux/Mac
ls -l /home/youruser/data/ds_finance_bank.mv.db
```

**Option B: Use WildFly Data Directory**

Store database inside WildFly installation:

```xml
<connection-url>jdbc:h2:${jboss.server.data.dir}/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>
```

This creates the database in: `$WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db`

---

## ✅ Solution 2: Verify Hibernate Configuration

Double-check your `persistence.xml`:

**File:** `ds-finance-bank-ejb/src/main/resources/META-INF/persistence.xml`

```xml
<properties>
  <!-- This should be 'update' NOT 'create' or 'create-drop' -->
  <property name="hibernate.hbm2ddl.auto" value="update"/>
  
  <!-- Optional: Enable SQL logging to see what's happening -->
  <property name="hibernate.show_sql" value="true"/>
  <property name="hibernate.format_sql" value="true"/>
</properties>
```

### What Each Value Does:

| Value | Behavior | Data Persistence |
|-------|----------|------------------|
| `create` | Drop and recreate tables on startup | ❌ Data LOST |
| `create-drop` | Create on startup, drop on shutdown | ❌ Data LOST |
| `update` | Create/update tables, keep data | ✅ Data PRESERVED |
| `validate` | Only validate schema, no changes | ✅ Data PRESERVED |
| `none` | Do nothing | ✅ Data PRESERVED |

**Your current setting is correct (`update`), so the issue is likely the database file path.**

---

## ✅ Solution 3: Check File Permissions

### Windows
```cmd
# Check if file exists
dir %USERPROFILE%\ds_finance_bank.mv.db

# If it exists, check if it's read-only
attrib %USERPROFILE%\ds_finance_bank.mv.db

# Remove read-only if needed
attrib -r %USERPROFILE%\ds_finance_bank.mv.db
```

### Linux/Mac
```bash
# Check if file exists
ls -l ~/ds_finance_bank.mv.db

# Fix permissions
chmod 644 ~/ds_finance_bank.mv.db
```

---

## ✅ Solution 4: Disable AUTO_SERVER (If Multiple Instances)

If you're running multiple WildFly instances or accessing the database from multiple tools:

```xml
<!-- Remove AUTO_SERVER if you only have one WildFly -->
<connection-url>jdbc:h2:/path/to/ds_finance_bank</connection-url>
```

Or use file locking mode:
```xml
<connection-url>jdbc:h2:/path/to/ds_finance_bank;FILE_LOCK=FS;AUTO_SERVER=TRUE</connection-url>
```

---

## ✅ Solution 5: Check for H2 Version Compatibility

H2 database changed file format between versions:
- **Old format:** `ds_finance_bank.h2.db` (single file)
- **New format:** `ds_finance_bank.mv.db` + `ds_finance_bank.trace.db` (multiple files)

If you're switching H2 versions, the old database file might be ignored.

### Check H2 Version in WildFly:
```bash
find $WILDFLY_HOME -name "h2*.jar"
```

### Force specific file format:
```xml
<!-- For old H2 format -->
<connection-url>jdbc:h2:/path/to/ds_finance_bank;MV_STORE=FALSE</connection-url>

<!-- For new H2 format (default) -->
<connection-url>jdbc:h2:/path/to/ds_finance_bank;MV_STORE=TRUE</connection-url>
```

---

## 🔍 Debugging: Find Where H2 is Storing Data

### Enable H2 Trace Logging

Add to connection URL:
```xml
<connection-url>jdbc:h2:/path/to/ds_finance_bank;TRACE_LEVEL_FILE=4;AUTO_SERVER=TRUE</connection-url>
```

This creates `ds_finance_bank.trace.db` with detailed logs.

### Check WildFly Logs

```bash
# Watch server.log for database file location
tail -f $WILDFLY_HOME/standalone/log/server.log | grep -i "h2\|database\|jdbc"
```

Look for lines like:
```
INFO  [stdout] (ServerService Thread Pool) Opening Database: /actual/path/to/ds_finance_bank
```

### SQL Query to Check Database Location

Connect to H2 Console and run:
```sql
SELECT * FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME='DATABASE_PATH';
```

---

## 🧪 Testing the Fix

### Step 1: Clear Old Data
```bash
# Stop WildFly

# Remove old database files
rm ~/ds_finance_bank.*  # Linux/Mac
del %USERPROFILE%\ds_finance_bank.*  # Windows

# Or if using custom path
rm /your/custom/path/ds_finance_bank.*
```

### Step 2: Apply Fix

1. Update datasource with absolute path
2. Ensure `persistence.xml` has `hibernate.hbm2ddl.auto=update`
3. Start WildFly

### Step 3: Create Test Data

```java
// Create a customer via Employee GUI
// Customer: Test User, testuser, password
```

### Step 4: Verify Persistence

```bash
# Stop WildFly
# Check database file exists
ls -lh /your/path/ds_finance_bank.mv.db

# File should have non-zero size
# Example: -rw-r--r-- 1 user user 256K Jan 21 16:00 ds_finance_bank.mv.db
```

### Step 5: Test Restart

```bash
# Start WildFly again
# Login to Employee GUI
# Search for "Test User" - should still exist!
```

---

## 📋 Recommended Configuration

### For Development (Easy Access)

```xml
<datasource jndi-name="java:/datasources/DsFinanceBankDS"
            pool-name="DsFinanceBankDS" enabled="true" use-java-context="true">
  <connection-url>jdbc:h2:${jboss.server.data.dir}/ds_finance_bank;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1</connection-url>
  <driver>h2</driver>
  <security>
    <user-name>sa</user-name>
    <password>sa</password>
  </security>
</datasource>
```

Database location: `$WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db`

### For Production (Separate Location)

```xml
<datasource jndi-name="java:/datasources/DsFinanceBankDS"
            pool-name="DsFinanceBankDS" enabled="true" use-java-context="true">
  <connection-url>jdbc:h2:/opt/trading-bank/data/ds_finance_bank;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;FILE_LOCK=FS</connection-url>
  <driver>h2</driver>
  <security>
    <user-name>sa</user-name>
    <password>sa</password>
  </security>
</datasource>
```

### Key Parameters Explained

- `DB_CLOSE_DELAY=-1` - Keep DB open until JVM shutdown
- `AUTO_SERVER=TRUE` - Allow multiple connections
- `FILE_LOCK=FS` - Use file system locking (safer)
- `MV_STORE=TRUE` - Use modern storage engine (default)

---

## ⚠️ Common Mistakes

### Mistake 1: Using Relative Paths
```xml
<!-- ❌ DON'T -->
<connection-url>jdbc:h2:./ds_finance_bank</connection-url>
<connection-url>jdbc:h2:data/ds_finance_bank</connection-url>
```

Problem: Current working directory changes between runs.

### Mistake 2: In-Memory Mode
```xml
<!-- ❌ DON'T (unless you want temporary data) -->
<connection-url>jdbc:h2:mem:ds_finance_bank</connection-url>
```

Problem: Data only exists in RAM, lost on shutdown.

### Mistake 3: Wrong Hibernate Setting
```xml
<!-- ❌ DON'T -->
<property name="hibernate.hbm2ddl.auto" value="create"/>
<property name="hibernate.hbm2ddl.auto" value="create-drop"/>
```

Problem: Tables dropped on startup/shutdown.

---

## 🔧 Quick Fix Script

### Linux/Mac

```bash
#!/bin/bash
# fix-db-persistence.sh

WILDFLY_HOME="/path/to/wildfly-28.0.1.Final"
DB_PATH="${WILDFLY_HOME}/standalone/data"

# Stop WildFly
${WILDFLY_HOME}/bin/jboss-cli.sh --connect command=:shutdown

# Create data directory
mkdir -p "${DB_PATH}"

# Update datasource in standalone.xml
sed -i.bak \
  's|jdbc:h2:~/ds_finance_bank|jdbc:h2:${jboss.server.data.dir}/ds_finance_bank|g' \
  "${WILDFLY_HOME}/standalone/configuration/standalone.xml"

echo "✅ Fixed datasource configuration"
echo "Database will be stored in: ${DB_PATH}/ds_finance_bank.mv.db"

# Start WildFly
${WILDFLY_HOME}/bin/standalone.sh
```

### Windows (PowerShell)

```powershell
# fix-db-persistence.ps1

$WildflyHome = "C:\wildfly-28.0.1.Final"
$ConfigFile = "$WildflyHome\standalone\configuration\standalone.xml"
$BackupFile = "$ConfigFile.bak"

# Stop WildFly (if running)
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

# Backup config
Copy-Item $ConfigFile $BackupFile

# Fix datasource URL
(Get-Content $ConfigFile) -replace 
  'jdbc:h2:~/ds_finance_bank', 
  'jdbc:h2:${jboss.server.data.dir}/ds_finance_bank' |
  Set-Content $ConfigFile

Write-Host "✅ Fixed datasource configuration" -ForegroundColor Green
Write-Host "Database will be stored in: $WildflyHome\standalone\data\ds_finance_bank.mv.db"

# Start WildFly
Start-Process "$WildflyHome\bin\standalone.bat"
```

---

## 📊 Verify Everything is Working

### 1. Check Database File Exists

```bash
# Should see files like:
# ds_finance_bank.mv.db (main database)
# ds_finance_bank.trace.db (log file, optional)

ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank*
```

### 2. Monitor File Size Growth

```bash
# Watch database file size increase when you add data
watch -n 2 'ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db'
```

### 3. Check Hibernate Logs

In WildFly console, you should see:
```
Hibernate: 
    create table if not exists customer (...)
Hibernate: 
    create table if not exists depot (...)
```

**NOT:**
```
Hibernate: drop table if exists customer
```

---

## 🎯 Summary

**Most Likely Fix:** Change datasource URL from `~/ds_finance_bank` to absolute path:

```xml
<connection-url>jdbc:h2:${jboss.server.data.dir}/ds_finance_bank;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1</connection-url>
```

**File Location:** `$WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db`

**Expected Behavior After Fix:**
1. Create customer → restart WildFly → customer still exists ✅
2. Database file size increases when you add data ✅
3. No "drop table" messages in logs ✅

---

**Last Updated:** January 21, 2026  
**Status:** ✅ Ready to apply
