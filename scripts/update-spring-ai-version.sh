#!/bin/bash

# Update Spring AI version across all pom.xml files
# Usage: ./scripts/update-spring-ai-version.sh [VERSION]
# Example: ./scripts/update-spring-ai-version.sh 1.0.1
# Example: ./scripts/update-spring-ai-version.sh 1.1.0-SNAPSHOT
# Example: ./scripts/update-spring-ai-version.sh 2.0.0-RC1

set -e

# Require exactly one version parameter
if [ "$#" -ne 1 ]; then
    echo "Error: exactly one Spring AI version is required." >&2
    echo "Usage: $0 <version>" >&2
    echo "Example: $0 2.0.0-SNAPSHOT" >&2
    exit 2
fi

VERSION="$1"

# Validate version format
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-(SNAPSHOT|RC[0-9]+|M[0-9]+))?$ ]]; then
    echo "Error: Invalid version format. Expected: X.Y.Z, X.Y.Z-SNAPSHOT, X.Y.Z-RC1, or X.Y.Z-M1"
    echo "Example: 2.0.0 or 2.0.0-SNAPSHOT or 2.0.0-RC1 or 2.0.0-M1"
    exit 1
fi

echo "Updating Spring AI version to: $VERSION"

# Find script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Change to project root
cd "${PROJECT_ROOT}"

# Create backup directory with timestamp
BACKUP_DIR="${PROJECT_ROOT}/.version-backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "${BACKUP_DIR}"

# Counter for updated files
UPDATED_COUNT=0
TOTAL_COUNT=0

# Find and process all pom.xml files
echo "Searching for pom.xml files..."
while IFS= read -r pom_file; do
    # Skip backup directories
    if [[ "$pom_file" == *".version-backups"* ]]; then
        continue
    fi
    
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    updated_file=false
    
    # Check if this pom file contains spring-ai.version property
    if grep -q "<spring-ai.version>" "$pom_file"; then
        # Create backup
        relative_path="${pom_file#./}"
        backup_file="${BACKUP_DIR}/${relative_path}"
        mkdir -p "$(dirname "$backup_file")"
        cp "$pom_file" "$backup_file"
        
        # Update the version property
        sed -i '' "s|<spring-ai.version>.*</spring-ai.version>|<spring-ai.version>${VERSION}</spring-ai.version>|g" "$pom_file"
        updated_file=true
    fi
    
    # Also check for direct spring-ai-bom version in dependencyManagement
    if grep -q "spring-ai-bom" "$pom_file"; then
        if ! $updated_file; then
            # Create backup if not already done
            relative_path="${pom_file#./}"
            backup_file="${BACKUP_DIR}/${relative_path}"
            mkdir -p "$(dirname "$backup_file")"
            cp "$pom_file" "$backup_file"
        fi
        
        # Update the BOM version using perl (cross-platform multiline replacement)
        perl -i -0pe 's|(<artifactId>spring-ai-bom</artifactId>\s*<version>)[^<]*(</version>)|${1}'"${VERSION}"'${2}|g' "$pom_file"
        
        updated_file=true
    fi
    
    if $updated_file; then
        echo "  ✓ Updated: ${pom_file#./}"
        UPDATED_COUNT=$((UPDATED_COUNT + 1))
    fi
done < <(find . -name "pom.xml" -type f)

echo ""
echo "Summary:"
echo "  Total pom.xml files found: $TOTAL_COUNT"
echo "  Files updated: $UPDATED_COUNT"
echo "  Backup location: $BACKUP_DIR"
echo ""

# Verify the update
echo "Verification - checking a sample file:"
sample_file="./misc/openai-streaming-response/pom.xml"
if [ -f "$sample_file" ]; then
    grep "<spring-ai.version>" "$sample_file" || echo "Sample file not found or doesn't contain spring-ai.version"
fi

echo ""
echo "✅ Spring AI version update complete!"
echo ""
echo "To restore from backup, run:"
echo "  ./scripts/restore-spring-ai-version.sh $BACKUP_DIR"