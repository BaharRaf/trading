# WildFly User Setup - Employee and Customer Roles

## Problem

You get this error when trying to create customers:
```
WFLYEJB0364: Invocation on method createCustomer() is not allowed
```

This means the user you're logged in as **doesn't have the "employee" role**.

---

## Solution: Create Employee User

### Option 1: Using add-user Script (Recommended)

1. **Open Command Prompt**:
   ```bash
   cd C:\Programs\wildfly-28.0.1.Final-dev\bin
   ```

2. **Run add-user.bat**:
   ```bash
   add-user.bat
   ```

3. **Follow the prompts**:
   ```
   What type of user do you wish to add?
   a) Management User
   b) Application User
   (a): b

   Enter the details of the new user to add.
   Username: employee
   Password: employeepass
   Re-enter Password: employeepass

   What groups do you want this user to belong to? (comma separated, or blank for none)
   [  ]: employee

   About to add user 'employee' for realm 'ApplicationRealm'
   Is this correct yes/no? yes
   ```

4. **Important**: When asked:
   ```
   Is this new user going to be used for one AS process to connect to another AS process?
   yes/no? no
   ```

5. **Verify files were updated**:
   - `wildfly/standalone/configuration/application-users.properties` should contain: `employee=<hash>`
   - `wildfly/standalone/configuration/application-roles.properties` should contain: `employee=employee`

---

### Option 2: Manual File Edit

1. **Stop WildFly** (Ctrl+C)

2. **Edit `application-roles.properties`**:
   ```
   C:\Programs\wildfly-28.0.1.Final-dev\standalone\configuration\application-roles.properties
   ```

3. **Add this line**:
   ```properties
   employee=employee
   ```

4. **Edit `application-users.properties`**:
   ```
   C:\Programs\wildfly-28.0.1.Final-dev\standalone\configuration\application-users.properties
   ```

5. **Add this line** (pre-generated hash for password "employeepass"):
   ```properties
   employee=b7e4c64d46de6490e2b4a87f8c6b994e
   ```

6. **Restart WildFly**:
   ```bash
   standalone.bat
   ```

---

## Required Users for the Application

### 1. Employee User (for Employee GUI)
- **Username**: `employee`
- **Password**: `employeepass`
- **Role**: `employee`
- **Purpose**: Access Employee GUI to create customers, manage trades

### 2. Customer User (for Customer GUI)
- **Username**: `customer` (or created by employee)
- **Password**: `customerpass` (or set by employee)
- **Role**: `customer`
- **Purpose**: Access Customer GUI to view portfolio

---

## Complete Setup Commands

**Run these if you want to set up BOTH users quickly:**

```bash
cd C:\Programs\wildfly-28.0.1.Final-dev\bin

# Create employee user
add-user.bat -a -u employee -p employeepass -g employee

# Create initial customer user (for testing)
add-user.bat -a -u customer -p customerpass -g customer
```

**Parameters explained:**
- `-a`: Application user (not management)
- `-u`: Username
- `-p`: Password
- `-g`: Groups/roles

---

## Verify User Setup

### Check Files

**application-roles.properties** should contain:
```properties
#$REALM_NAME=ApplicationRealm$ This line is used by the add-user utility to identify the realm name already used in this file.
employee=employee
customer=customer
```

**application-users.properties** should contain (hashes will differ):
```properties
#$REALM_NAME=ApplicationRealm$ This line is used by the add-user utility to identify the realm name already used in this file.
employee=b7e4c64d46de6490e2b4a87f8c6b994e
customer=d426fc24c595e7e37faa4224e6e15b2b
```

### Test Login

1. **Launch Employee GUI**:
   ```bash
   java -jar ds-finance-bank-client-2.0-SNAPSHOT-jar-with-dependencies.jar
   ```

2. **Select "Employee GUI"**

3. **Login with**:
   - Username: `employee`
   - Password: `employeepass`

4. **Try creating a customer** - should work now!

---

## Troubleshooting

### Error: "Authentication failed"
**Cause**: Wrong username/password or user doesn't exist

**Solution**: Re-run add-user.bat or check the properties files

### Error: "Invocation not allowed" (WFLYEJB0364)
**Cause**: User doesn't have the correct role

**Solution**: 
1. Check `application-roles.properties`
2. Make sure the line `employee=employee` exists
3. Restart WildFly

### Error: "User already exists"
**Cause**: User was already created

**Solution**: 
```bash
# Remove user
add-user.bat -a -u employee -r

# Then add again
add-user.bat -a -u employee -p employeepass -g employee
```

### Users created by Employee GUI can't login
**Cause**: This is expected! The `WildflyAuthDBHelper` we implemented automatically creates customer users.

**How it works**:
1. Employee creates customer via GUI
2. Customer saved to database
3. `WildflyAuthDBHelper` creates WildFly user with "customer" role
4. Customer can immediately login to Customer GUI

---

## Security Notes

### Roles Explained

**employee** role:
- Access to `EmployeeBankService`
- Can create customers
- Can manage stock trades
- Can view any customer's portfolio
- Can search customers

**customer** role:
- Access to `CustomerBankService`
- Can only view their own portfolio
- Can buy/sell their own stocks
- Can search stock quotes

### Default Credentials (Change in Production!)

For **development/testing**:
- Employee: `employee` / `employeepass`
- Customer: `customer` / `customerpass`

For **production**, use strong passwords:
```bash
add-user.bat -a -u employee -p "MyStr0ng!P@ssw0rd" -g employee
```

---

## Quick Reference

### Add User Command Format
```bash
add-user.bat -a -u <username> -p <password> -g <role>
```

### Common Commands
```bash
# List all users (view files directly)
type C:\Programs\wildfly-28.0.1.Final-dev\standalone\configuration\application-users.properties
type C:\Programs\wildfly-28.0.1.Final-dev\standalone\configuration\application-roles.properties

# Remove user
add-user.bat -a -u <username> -r

# Change password (remove then re-add)
add-user.bat -a -u employee -r
add-user.bat -a -u employee -p newpassword -g employee
```

---

## What to Do Now

1. **Create employee user** (if not done):
   ```bash
   cd C:\Programs\wildfly-28.0.1.Final-dev\bin
   add-user.bat -a -u employee -p employeepass -g employee
   ```

2. **Restart WildFly** (if it was running):
   - Press Ctrl+C in the WildFly terminal
   - Run `standalone.bat` again

3. **Launch Employee GUI** and login with:
   - Username: `employee`
   - Password: `employeepass`

4. **Create customers** - they will automatically get the "customer" role!

---

**Created**: January 21, 2026  
**Repository**: [BaharRaf/trading](https://github.com/BaharRaf/trading)  