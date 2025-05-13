#!/bin/bash

# Script to create a new Flyway migration file with timestamp-based versioning.

# Check for correct number of arguments
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <module_name|global> \"<Migration Description>\""
    echo "  <module_name>: The name of the specific module (e.g., 'clientapplication', 'user')."
    echo "  global: Use the keyword 'global' for non-module-specific migrations (creates in 'db/migration/')."
    echo "          Do NOT use 'global' as an actual module name for 'db/modules/global/'."
    echo "Example (module): $0 clientapplication \"Add new feature for clients\""
    echo "Example (global): $0 global \"Update shared lookup table\""
    exit 1
fi

MODULE_NAME=$1
MIGRATION_DESCRIPTION=$2

# Generate timestamp (YYYYMMDDHHMMSS format, UTC)
# Using -u for UTC to ensure consistency across different timezones
TIMESTAMP=$(date -u +'%Y%m%d%H%M%S')

# Replace spaces and special characters in description with underscores
SANITIZED_DESCRIPTION=$(echo "$MIGRATION_DESCRIPTION" | tr -s '[:space:]' '_' | tr -cs '[:alnum:]_' '_')

# Remove leading/trailing underscores that might result from sanitization
SANITIZED_DESCRIPTION=$(echo "$SANITIZED_DESCRIPTION" | sed 's/^_//;s/_$//')


FILENAME="V${TIMESTAMP}__${SANITIZED_DESCRIPTION}.sql"
BASE_PATH="src/main/resources/db"

# Determine target directory
if [ "$MODULE_NAME" == "global" ]; then
    TARGET_DIR="${BASE_PATH}/migration"
else
    TARGET_DIR="${BASE_PATH}/modules/${MODULE_NAME}"
fi

FULL_PATH="${TARGET_DIR}/${FILENAME}"

# Create target directory if it doesn't exist
mkdir -p "$TARGET_DIR"

# Create the empty migration file
touch "$FULL_PATH"

echo "Successfully created migration file:"
echo "$FULL_PATH"
echo ""
echo "Please add your SQL statements to this file."

exit 0
