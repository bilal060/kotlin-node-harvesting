#!/bin/bash

echo "🚀 DeviceSync System - Quick Start Script"
echo "=========================================="

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 16+ first."
    exit 1
fi

# Check npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm not found. Please install npm first."
    exit 1
fi

# Check MongoDB connection
echo "🔍 Checking MongoDB connection..."

# Check if we can connect to MongoDB (basic check)
if ! command -v mongo &> /dev/null && ! command -v mongosh &> /dev/null; then
    echo "⚠️  MongoDB client not found. Make sure MongoDB is running."
fi

echo "✅ Prerequisites check completed"

# Backend Setup
echo ""
echo "🔧 Setting up Backend..."
cd Backend

if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp env.example .env
    echo "⚠️  Please edit Backend/.env with your MongoDB URI and other settings"
else
    echo "✅ .env file already exists"
fi

echo "📦 Installing backend dependencies..."
npm install

echo "✅ Backend setup completed"

# Frontend Setup
echo ""
echo "🔧 Setting up Frontend..."
cd ../frontend

echo "📦 Installing frontend dependencies..."
npm install

if [ ! -f .env.local ]; then
    echo "📝 Creating .env.local file..."
    echo "NEXT_PUBLIC_API_URL=http://localhost:5001" > .env.local
    echo "✅ Frontend environment configured"
else
    echo "✅ .env.local file already exists"
fi

echo "✅ Frontend setup completed"

# Android App Setup
echo ""
echo "🔧 Setting up Android App..."
cd ../kotlin

if [ -f "gradlew" ]; then
    echo "📱 Building Android APK..."
    ./gradlew assembleDebug
    echo "✅ APK built successfully"
    echo "📁 APK location: kotlin/app/build/outputs/apk/debug/"
else
    echo "⚠️  Gradle wrapper not found. Please run 'gradle wrapper' first."
fi

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Edit Backend/.env with your MongoDB connection string"
echo "2. Start backend: cd Backend && npm run dev"
echo "3. Start frontend: cd frontend && npm run dev"
echo "4. Install APK on Android device from: kotlin/app/build/outputs/apk/debug/"
echo ""
echo "🌐 Backend will run on: http://localhost:5001"
echo "🖥️  Frontend will run on: http://localhost:3000"
echo ""
echo "Happy coding! 🚀" 