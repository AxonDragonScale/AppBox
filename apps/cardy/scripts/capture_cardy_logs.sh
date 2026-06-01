#!/bin/bash
# Capture Cardy app logs from adb and save to build/adb_log/
# Usage: ./apps/cardy/scripts/capture_cardy_logs.sh
# Stop with Ctrl+C — logs are saved continuously.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$PROJECT_DIR/build/adb_log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/cardy_${TIMESTAMP}.log"

mkdir -p "$LOG_DIR"

# Cardy-specific tags for filtering
TAGS="CardyActivity|HomeVM|HomeScreen|NfcManager|CardReader|EmvDataExtractor|ReaderVM"

echo "=== Cardy ADB Log Capture ==="
echo "Output: $LOG_FILE"
echo "Filter: $TAGS"
echo "Press Ctrl+C to stop"
echo ""

# Clear logcat buffer first
adb logcat -c 2>/dev/null

# Capture with tag filter, write to file and stdout
adb logcat -v time | grep -E "$TAGS" | tee "$LOG_FILE"
