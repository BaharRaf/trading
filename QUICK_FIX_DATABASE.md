# ⚡ QUICK FIX: Database Resets on Restart

## Problem
Every time you restart WildFly, all your data (customers, depots, etc.) is lost.

## 🔴 5-Minute Fix

### Option 1: Automatic Script (Recommended)

#### Linux/Mac:
```bash
cd scripts
chmod +x fix-database-persistence.sh
./fix-database-persistence.sh
```

#### Windows:
```cmd
cd scripts
fix-database-persistence.bat
```

Then restart WildFly. **Done!**

---

### Option 2: Manual Fix

1. **Stop WildFly**

2. **Open this file in a text editor:**
   ```
   $WILDFLY_HOME/standalone/configuration/standalone.xml
   ```

3. **Find this line** (search for "DsFinanceBankDS"):
   ```xml
   <connection-url>jdbc:h2:~/ds_finance_bank;AUTO_SERVER=TRUE</connection-url>
   ```

4. **Replace it with:**
   ```xml
   <connection-url>jdbc:h2:${jboss.server.data.dir}/ds_finance_bank;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1</connection-url>
   ```

5. **Save the file**

6. **Start WildFly**

7. **Test it:**
   - Create a customer
   - Restart WildFly
   - Customer should still exist! ✅

---

## What This Does

**Before:** Database was stored at `~/ds_finance_bank` (might not work correctly)  
**After:** Database is stored at `$WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db` (always works)

---

## Verify It's Working

### 1. Check Database File

**Windows:**
```cmd
dir %WILDFLY_HOME%\standalone\data\ds_finance_bank.mv.db
```

**Linux/Mac:**
```bash
ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db
```

You should see a file with **non-zero size**.

### 2. Test Persistence

1. Start WildFly
2. Create a customer (any name)
3. Note the file size:
   ```bash
   ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db
   # Example: 192K
   ```
4. **Stop WildFly**
5. Check file still exists and has same/larger size
6. **Start WildFly again**
7. Search for the customer → **should be found!** ✅

---

## Troubleshooting

### "File not found after fix"

1. Check WildFly logs:
   ```bash
   tail -f $WILDFLY_HOME/standalone/log/server.log | grep -i "h2\|database"
   ```

2. Look for line showing database path:
   ```
   INFO Opening Database: /actual/path/to/ds_finance_bank
   ```

### "Still losing data"

1. Check `persistence.xml` has:
   ```xml
   <property name="hibernate.hbm2ddl.auto" value="update"/>
   ```
   
   **NOT** `create` or `create-drop`

2. Make sure WildFly has write permissions to `standalone/data/` directory

---

## Full Documentation

For detailed explanation and advanced options, see:
- `DATABASE_PERSISTENCE_FIX.md` - Complete troubleshooting guide
- `config/datasource-persistent.xml` - Full datasource configuration

---

**Expected Result:** Database persists across WildFly restarts! ✅
