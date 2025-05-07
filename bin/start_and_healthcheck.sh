#!/bin/bash

# Script to start Spring Boot app, healthcheck it, and manage its lifecycle.

# Ensure we are in the project root directory so mvn commands work as expected
# This is important if the script is called from a different directory.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

LOG_PREFIX="[start_and_healthcheck]"
SERVER_PID_FILE=".server.pid"
HEALTHCHECK_URL="http://localhost:8080/v1/status"
TIMEOUT_SECONDS=30
POLL_INTERVAL_SECONDS=2

# Function to clean up: kill the server process and its children
cleanup() {
    echo "$LOG_PREFIX Cleaning up..."
    if [ -f "$SERVER_PID_FILE" ]; then
        SERVER_PID=$(cat "$SERVER_PID_FILE")
        if ps -p "$SERVER_PID" > /dev/null; then
            echo "$LOG_PREFIX Stopping Spring Boot application (PID: $SERVER_PID)..."
            # On macOS, we don't have process groups like Linux, so kill the PID directly
            kill -TERM "$SERVER_PID" 2>/dev/null || kill -KILL "$SERVER_PID" 2>/dev/null
            # Wait a moment for the process to terminate
            sleep 2
            if ps -p "$SERVER_PID" > /dev/null; then
                echo "$LOG_PREFIX Server (PID: $SERVER_PID) did not terminate gracefully, forcing kill."
                kill -KILL "$SERVER_PID" 2>/dev/null
            else
                echo "$LOG_PREFIX Server (PID: $SERVER_PID) stopped."
            fi
        else
            echo "$LOG_PREFIX Server process (PID: $SERVER_PID) not found or already stopped."
        fi
        rm -f "$SERVER_PID_FILE"
    else
        echo "$LOG_PREFIX PID file not found. No server process to stop explicitly via PID file."
        # As a fallback, try to find and kill any lingering Spring Boot processes on port 8080
        # This is a bit more aggressive and less precise
        LSOF_PID=$(lsof -t -i:8080 -sTCP:LISTEN)
        if [ -n "$LSOF_PID" ]; then
            echo "$LOG_PREFIX Found process $LSOF_PID listening on port 8080. Attempting to kill it."
            kill -TERM "$LSOF_PID" 2>/dev/null || kill -KILL "$LSOF_PID" 2>/dev/null
            sleep 1
            if ps -p "$LSOF_PID" > /dev/null; then
                 echo "$LOG_PREFIX Process $LSOF_PID did not terminate, forcing kill."
                 kill -KILL "$LSOF_PID" 2>/dev/null
            else
                echo "$LOG_PREFIX Process $LSOF_PID listening on port 8080 stopped."
            fi
        fi    
    fi
    echo "$LOG_PREFIX Cleanup finished."
}

# Trap EXIT, INT, TERM signals to ensure cleanup is called
trap cleanup EXIT INT TERM

# Start the Spring Boot application in the background
echo "$LOG_PREFIX Starting Spring Boot application..."
# On macOS, we'll use a simpler approach for background processes
mvn clean spring-boot:run > mvn_output.log 2>&1 & 
echo $! > "$SERVER_PID_FILE"

# Brief pause to allow the server to attempt to start and PID file to be written
sleep 3

# Display the Maven output in the background so we can see the logs
(tail -f mvn_output.log &)
TAIL_PID=$!

if [ ! -f "$SERVER_PID_FILE" ]; then
    echo "$LOG_PREFIX ERROR: PID file $SERVER_PID_FILE was not created. Server may have failed to start." >&2
    kill $TAIL_PID 2>/dev/null
    exit 1
fi

SERVER_PID=$(cat "$SERVER_PID_FILE")

if ! ps -p "$SERVER_PID" > /dev/null; then
    echo "$LOG_PREFIX ERROR: Server process (PID: $SERVER_PID) is not running. Check Maven logs." >&2
    rm -f "$SERVER_PID_FILE"
    kill $TAIL_PID 2>/dev/null
    exit 1
fi

echo "$LOG_PREFIX Server started with PID: $SERVER_PID. Waiting for application to be ready..."

# Poll the healthcheck endpoint
SECONDS_WAITED=0
while [ "$SECONDS_WAITED" -lt "$TIMEOUT_SECONDS" ]; do    
    # Check if the server process is still alive
    if ! ps -p "$SERVER_PID" > /dev/null; then
        echo "$LOG_PREFIX ERROR: Server process (PID: $SERVER_PID) died unexpectedly. Check application logs." >&2
        kill $TAIL_PID 2>/dev/null
        exit 1
    fi

    HTTP_STATUS=$(curl --silent --output /dev/null --write-out "%{http_code}" "$HEALTHCHECK_URL")

    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "$LOG_PREFIX Healthcheck successful (HTTP 200). Application is ready."
        # Kill the tail process before exiting
        kill $TAIL_PID 2>/dev/null
        # The trap will handle cleanup and process termination
        exit 0
    fi
    
    echo "$LOG_PREFIX Healthcheck attempt: $(($SECONDS_WAITED / $POLL_INTERVAL_SECONDS + 1)). Status: $HTTP_STATUS. Waiting... ($SECONDS_WAITED/$TIMEOUT_SECONDS s)"
    sleep "$POLL_INTERVAL_SECONDS"
    SECONDS_WAITED=$((SECONDS_WAITED + POLL_INTERVAL_SECONDS))
done

# Timeout reached
echo "$LOG_PREFIX ERROR: Timeout ($TIMEOUT_SECONDS seconds) reached waiting for application to be ready." >&2
echo "$LOG_PREFIX Last healthcheck status was: $HTTP_STATUS" >&2
# Kill the tail process before exiting
kill $TAIL_PID 2>/dev/null
# The trap will handle cleanup and process termination
exit 1