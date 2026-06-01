#!/bin/bash
# Dump recent Cardy logs from adb logcat buffer to build/adb_log/
# Usage: ./apps/cardy/scripts/dump_cardy_logs.sh
# This captures the current logcat buffer (not live streaming).

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$PROJECT_DIR/build/adb_log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/cardy_dump_${TIMESTAMP}.log"

mkdir -p "$LOG_DIR"

# Cardy-specific tags
TAGS="CardyActivity|HomeVM|HomeScreen|NfcManager|CardReader|EmvDataExtractor|ReaderVM"

echo "=== Cardy ADB Log Dump ==="
echo "Output: $LOG_FILE"

# Dump logcat buffer filtered by Cardy tags
adb logcat -d -v time | grep -E "$TAGS" > "$LOG_FILE"

LINE_COUNT=$(wc -l < "$LOG_FILE")
echo "Captured $LINE_COUNT lines to $LOG_FILE"

# Also print the last 50 lines for quick inspection
echo ""
echo "--- Last 50 lines ---"
tail -50 "$LOG_FILE"
