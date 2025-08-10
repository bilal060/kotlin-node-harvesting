#!/bin/bash

# DeviceSync Backend Server Stop Script

echo "🛑 Stopping DeviceSync Backend Server..."

# Find the process using port 5001
PID=$(lsof -ti :5001)

if [ -z "$PID" ]; then
    echo "✅ No server running on port 5001"
    exit 0
fi

echo "🔄 Stopping server process (PID: $PID)..."

# Try graceful shutdown first
kill $PID

# Wait for graceful shutdown
sleep 3

# Check if process is still running
if kill -0 $PID 2>/dev/null; then
    echo "⚠️  Graceful shutdown failed. Force stopping..."
    kill -9 $PID
    sleep 1
fi

# Verify process is stopped
if kill -0 $PID 2>/dev/null; then
    echo "❌ Failed to stop server process"
    exit 1
else
    echo "✅ Server stopped successfully"
fi

# Check if port is free
if lsof -i :5001 > /dev/null 2>&1; then
    echo "⚠️  Port 5001 is still in use. Checking for other processes..."
    lsof -i :5001
else
    echo "✅ Port 5001 is now free"
fi 