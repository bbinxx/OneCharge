#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status (except in log tail)
set -e

# ── Colors ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

# ── App details ──────────────────────────────────────────────────────────────
PACKAGE_NAME="com.bbinxx.onecharge"
ACTIVITY_NAME="${PACKAGE_NAME}.MainActivity"

# ── Usage ────────────────────────────────────────────────────────────────────
function print_usage() {
    echo -e "${BLUE}${BOLD}Usage:${NC} ./run.sh [options]"
    echo ""
    echo "Options:"
    echo "  -c, --clean        Clean build before running"
    echo "  -l, --logs         Stream logs after launch (default: on)"
    echo "  -n, --no-logs      Skip log streaming"
    echo "  -L, --logs-only    Only stream logs, skip build+install"
    echo "  -h, --help         Show this help message"
    echo ""
    echo "Log level colors:"
    echo "  Verbose=white  Debug=cyan  Info=green  Warn=yellow  Error=red"
}

# ── Defaults ─────────────────────────────────────────────────────────────────
CLEAN_BUILD=false
STREAM_LOGS=true       # logs are ON by default now
LOGS_ONLY=false

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--clean)     CLEAN_BUILD=true ;;
        -l|--logs)      STREAM_LOGS=true ;;
        -n|--no-logs)   STREAM_LOGS=false ;;
        -L|--logs-only) LOGS_ONLY=true ;;
        -h|--help)      print_usage; exit 0 ;;
        *) echo -e "${RED}Unknown parameter:${NC} $1"; print_usage; exit 1 ;;
    esac
    shift
done

echo -e "${CYAN}${BOLD}=============================================${NC}"
echo -e "${CYAN}${BOLD}      OneCharge Android Run Script          ${NC}"
echo -e "${CYAN}${BOLD}=============================================${NC}"

# ── Log streaming helper ─────────────────────────────────────────────────────
function stream_logs() {
    local pid="$1"

    echo ""
    echo -e "${CYAN}${BOLD}─── Live Logs ─────────────────────────────────${NC}"
    echo -e "${BLUE}[*] Streaming logs for: ${PACKAGE_NAME} (PID: ${pid:-auto})${NC}"
    echo -e "${BLUE}[*] Press ${BOLD}Ctrl+C${NC}${BLUE} to stop.${NC}"
    echo ""

    # Clear stale logs
    adb logcat -c 2>/dev/null || true
    sleep 0.5

    # Resolve PID if not provided
    if [ -z "$pid" ]; then
        pid=$(adb shell pidof -s "$PACKAGE_NAME" 2>/dev/null || true)
    fi

    # Colorise log lines by level
    colorise_logs() {
        while IFS= read -r line; do
            # Detect log level from logcat format:  MM-DD HH:MM:SS.mmm PID TID LEVEL TAG: msg
            level=$(echo "$line" | grep -oP '(?<=\d{2}:\d{2}:\d{2}\.\d{3}\s+\d+\s+\d+\s)[VDIWEF]' || true)
            case "$level" in
                V) echo -e "${NC}${line}"   ;;   # Verbose – plain
                D) echo -e "${CYAN}${line}${NC}"  ;;   # Debug   – cyan
                I) echo -e "${GREEN}${line}${NC}" ;;   # Info    – green
                W) echo -e "${YELLOW}${line}${NC}";;   # Warn    – yellow
                E) echo -e "${RED}${BOLD}${line}${NC}" ;;   # Error   – bold red
                F) echo -e "${RED}${BOLD}${line}${NC}" ;;   # Fatal   – bold red
                *) echo "$line" ;;
            esac
        done
    }

    if [ -n "$pid" ]; then
        # PID-filtered stream (cleanest)
        adb logcat --pid="$pid" 2>/dev/null | colorise_logs
    else
        # Fallback: grep by package name  
        adb logcat 2>/dev/null \
            | grep --line-buffered -E "(${PACKAGE_NAME}|AndroidRuntime|FATAL)" \
            | colorise_logs
    fi
}

# ── Logs-only mode ───────────────────────────────────────────────────────────
if [ "$LOGS_ONLY" = true ]; then
    echo -e "${BLUE}[*] Log-only mode – skipping build and install.${NC}"
    stream_logs ""
    exit 0
fi

# ── Check for devices ────────────────────────────────────────────────────────
echo -e "${BLUE}[*] Checking for connected devices...${NC}"
DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" || true)

if [ -z "$DEVICES" ]; then
    echo -e "${YELLOW}[!] No connected devices found.${NC}"
    if command -v emulator > /dev/null 2>&1; then
        AVDS=$(emulator -list-avds 2>/dev/null || true)
        if [ -n "$AVDS" ]; then
            FIRST_AVD=$(echo "$AVDS" | head -n 1)
            echo -e "${YELLOW}[?] Starting emulator: $FIRST_AVD ...${NC}"
            emulator -avd "$FIRST_AVD" &
            adb wait-for-device
            echo -e "${GREEN}[✓] Emulator connected!${NC}"
        else
            echo -e "${RED}[✗] No emulators found. Connect a device or start an emulator.${NC}"
            exit 1
        fi
    else
        echo -e "${RED}[✗] Connect an Android device or start an emulator.${NC}"
        exit 1
    fi
else
    DEVICE_COUNT=$(echo "$DEVICES" | wc -l)
    echo -e "${GREEN}[✓] Found ${DEVICE_COUNT} device(s):${NC}"
    echo "$DEVICES"
fi

# ── Clean build (optional) ────────────────────────────────────────────────────
if [ "$CLEAN_BUILD" = true ]; then
    echo -e "${BLUE}[*] Cleaning previous build...${NC}"
    ./gradlew clean
fi

# ── Build & install ───────────────────────────────────────────────────────────
echo -e "${BLUE}[*] Building and installing debug APK...${NC}"
./gradlew installDebug

# ── Launch app ────────────────────────────────────────────────────────────────
echo -e "${BLUE}[*] Launching ${ACTIVITY_NAME}...${NC}"
adb shell am start -n "${PACKAGE_NAME}/${ACTIVITY_NAME}"

echo -e "${GREEN}[✓] App launched!${NC}"

# ── Stream logs ───────────────────────────────────────────────────────────────
if [ "$STREAM_LOGS" = true ]; then
    # Give the process a moment to initialize before we grab the PID
    sleep 1.5
    PID=$(adb shell pidof -s "$PACKAGE_NAME" 2>/dev/null || true)
    stream_logs "$PID"
fi
