#!/bin/bash

# DeviceSync Backend Server Startup Script
# This script ensures clean startup by checking for existing processes

echo "🚀 Starting DeviceSync Backend Server..."

# Check if port 5001 is already in use
if lsof -i :5001 > /dev/null 2>&1; then
    echo "⚠️  Port 5001 is already in use. Checking for existing server process..."
    
    # Find the process using port 5001
    PID=$(lsof -ti :5001)
    if [ ! -z "$PID" ]; then
        echo "🔄 Stopping existing server process (PID: $PID)..."
        kill -9 $PID
        sleep 2
        echo "✅ Existing process stopped"
    fi
fi

# Wait a moment for port to be released
sleep 1

# Check if port is now free
if lsof -i :5001 > /dev/null 2>&1; then
    echo "❌ Port 5001 is still in use. Please check manually."
    exit 1
fi

echo "✅ Port 5001 is free. Starting server..."

# Start the server in the background
nohup npm start > server_output.log 2>&1 &
SERVER_PID=$!

echo "🔄 Server starting... (PID: $SERVER_PID)"

# Wait for server to start
sleep 5

# Check if server is running
if lsof -i :5001 > /dev/null 2>&1; then
    echo "✅ Server started successfully!"
    echo "📱 Server running on http://localhost:5001"
    echo "🏥 Health check: http://localhost:5001/api/health"
    echo "📊 Logs: tail -f server_output.log"
    echo "🛑 To stop: kill $SERVER_PID"
else
    echo "❌ Server failed to start. Check logs:"
    tail -10 server_output.log
    exit 1
fi 