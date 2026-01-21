#!/bin/bash
# Fix H2 Database Persistence Issue
# This script updates the WildFly datasource to use persistent storage

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "${GREEN}=== H2 Database Persistence Fix ===${NC}"
echo ""

# Check if WILDFLY_HOME is set
if [ -z "$WILDFLY_HOME" ]; then
    echo "${RED}ERROR: WILDFLY_HOME environment variable is not set${NC}"
    echo "Please set it to your WildFly installation directory:"
    echo "  export WILDFLY_HOME=/path/to/wildfly-28.0.1.Final"
    exit 1
fi

CONFIG_FILE="$WILDFLY_HOME/standalone/configuration/standalone.xml"
BACKUP_FILE="$CONFIG_FILE.backup-$(date +%Y%m%d-%H%M%S)"

echo "WildFly Home: $WILDFLY_HOME"
echo "Config File: $CONFIG_FILE"
echo ""

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "${RED}ERROR: standalone.xml not found at: $CONFIG_FILE${NC}"
    exit 1
fi

# Ask for confirmation
echo "${YELLOW}This script will:${NC}"
echo "1. Backup your current standalone.xml"
echo "2. Update the DsFinanceBankDS datasource connection URL"
echo "3. Database will be stored in: $WILDFLY_HOME/standalone/data/"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelled."
    exit 0
fi

# Create backup
echo "${GREEN}Creating backup...${NC}"
cp "$CONFIG_FILE" "$BACKUP_FILE"
echo "Backup saved to: $BACKUP_FILE"

# Update datasource URL
echo "${GREEN}Updating datasource configuration...${NC}"

# Replace the connection URL
sed -i.tmp \
  's|jdbc:h2:~/ds_finance_bank|jdbc:h2:${jboss.server.data.dir}/ds_finance_bank|g' \
  "$CONFIG_FILE"

# Also add DB_CLOSE_DELAY if not present
if grep -q "DB_CLOSE_DELAY" "$CONFIG_FILE"; then
    echo "DB_CLOSE_DELAY already configured"
else
    sed -i.tmp \
      's|\(${jboss.server.data.dir}/ds_finance_bank\)|\1;DB_CLOSE_DELAY=-1|g' \
      "$CONFIG_FILE"
fi

rm -f "$CONFIG_FILE.tmp"

echo "${GREEN}Done!${NC}"
echo ""
echo "${YELLOW}Next steps:${NC}"
echo "1. Restart WildFly"
echo "2. Database will now persist at:"
echo "   $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db"
echo "3. Your data will survive WildFly restarts!"
echo ""
echo "${YELLOW}To verify:${NC}"
echo "  - Create a customer"
echo "  - Stop WildFly"
echo "  - Check: ls -lh $WILDFLY_HOME/standalone/data/ds_finance_bank.mv.db"
echo "  - Start WildFly"
echo "  - Search for the customer - it should still exist!"
echo ""
echo "${GREEN}Backup location: $BACKUP_FILE${NC}"
echo "(You can restore from this backup if needed)"
