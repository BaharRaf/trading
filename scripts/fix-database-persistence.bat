@echo off
REM Fix H2 Database Persistence Issue (Windows)
REM This script updates the WildFly datasource to use persistent storage

setlocal enabledelayedexpansion

echo ========================================
echo H2 Database Persistence Fix (Windows)
echo ========================================
echo.

REM Check if WILDFLY_HOME is set
if "%WILDFLY_HOME%"=="" (
    echo ERROR: WILDFLY_HOME environment variable is not set
    echo Please set it to your WildFly installation directory:
    echo   set WILDFLY_HOME=C:\wildfly-28.0.1.Final
    pause
    exit /b 1
)

set CONFIG_FILE=%WILDFLY_HOME%\standalone\configuration\standalone.xml
set BACKUP_FILE=%CONFIG_FILE%.backup-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_FILE=%BACKUP_FILE: =0%

echo WildFly Home: %WILDFLY_HOME%
echo Config File: %CONFIG_FILE%
echo.

REM Check if config file exists
if not exist "%CONFIG_FILE%" (
    echo ERROR: standalone.xml not found at: %CONFIG_FILE%
    pause
    exit /b 1
)

REM Ask for confirmation
echo This script will:
echo 1. Backup your current standalone.xml
echo 2. Update the DsFinanceBankDS datasource connection URL
echo 3. Database will be stored in: %WILDFLY_HOME%\standalone\data\
echo.
set /p CONFIRM="Continue? (y/n): "
if /i not "%CONFIRM%"=="y" (
    echo Cancelled.
    pause
    exit /b 0
)

REM Create backup
echo Creating backup...
copy "%CONFIG_FILE%" "%BACKUP_FILE%" >nul
echo Backup saved to: %BACKUP_FILE%

REM Update datasource URL using PowerShell
echo Updating datasource configuration...

powershell -Command "(Get-Content '%CONFIG_FILE%') -replace 'jdbc:h2:~/ds_finance_bank', 'jdbc:h2:${jboss.server.data.dir}/ds_finance_bank' | Set-Content '%CONFIG_FILE%'"

powershell -Command "if ((Get-Content '%CONFIG_FILE%' | Select-String 'DB_CLOSE_DELAY').Length -eq 0) { (Get-Content '%CONFIG_FILE%') -replace '(\${jboss.server.data.dir}/ds_finance_bank)', '$1;DB_CLOSE_DELAY=-1' | Set-Content '%CONFIG_FILE%' }"

echo Done!
echo.
echo Next steps:
echo 1. Restart WildFly
echo 2. Database will now persist at:
echo    %WILDFLY_HOME%\standalone\data\ds_finance_bank.mv.db
echo 3. Your data will survive WildFly restarts!
echo.
echo To verify:
echo   - Create a customer
echo   - Stop WildFly
echo   - Check: dir %WILDFLY_HOME%\standalone\data\ds_finance_bank.mv.db
echo   - Start WildFly
echo   - Search for the customer - it should still exist!
echo.
echo Backup location: %BACKUP_FILE%
echo (You can restore from this backup if needed)
echo.
pause
