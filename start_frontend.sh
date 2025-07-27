#!/bin/bash

# Device Sync Frontend Dashboard Startup Script

echo "🚀 Starting Device Sync Frontend Dashboard..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js version 18+ is required. Current version: $(node -v)"
    exit 1
fi

echo "✅ Node.js version: $(node -v)"

# Navigate to frontend directory
cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install dependencies"
        exit 1
    fi
fi

# Check if .env.local exists, create if not
if [ ! -f ".env.local" ]; then
    echo "🔧 Creating environment configuration..."
    cat > .env.local << EOF
NEXT_PUBLIC_API_URL=https://kotlin-node-harvesting.onrender.com
EOF
    echo "✅ Environment file created"
fi

# Test backend connection
echo "🔍 Testing backend connection..."
BACKEND_RESPONSE=$(curl -s https://kotlin-node-harvesting.onrender.com/api/health)
if [[ $BACKEND_RESPONSE == *"success"* ]]; then
    echo "✅ Backend is accessible"
else
    echo "⚠️  Warning: Backend might not be accessible"
fi

# Start the development server
echo "🌐 Starting development server..."
echo "📱 Dashboard will be available at: http://localhost:3000"
echo "🔗 Backend API: https://kotlin-node-harvesting.onrender.com/api"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

npm run dev 