#!/bin/bash

echo "🚀 Setting up Environment Variables for Render Deployment"
echo "========================================================"

echo ""
echo "📋 Current MongoDB Configuration Status:"
echo "✅ All server files updated to use MONGODB_URI environment variable"
echo "✅ Database configuration improved for production"
echo "✅ Environment example file created: Backend/env.example"
echo ""

echo "🔧 Next Steps for Render Deployment:"
echo ""
echo "1. Set up MongoDB Atlas (if not already done):"
echo "   - Go to https://www.mongodb.com/atlas"
echo "   - Create free account and cluster"
echo "   - Get your connection string"
echo ""

echo "2. Configure Render Environment Variables:"
echo "   - Go to your Render dashboard"
echo "   - Select kotlin-node-harvesting service"
echo "   - Go to Environment tab"
echo "   - Add these environment variables:"
echo ""

echo "   📝 Required Variables:"
echo "   - MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/sync_data?retryWrites=true&w=majority"
echo "   - NODE_ENV=production"
echo "   - PORT=5001"
echo ""

echo "   📝 Optional Variables:"
echo "   - JWT_SECRET=your-secret-key"
echo "   - CORS_ORIGIN=https://your-frontend-domain.com"
echo "   - MAX_FILE_SIZE=10485760"
echo ""

echo "3. Redeploy your service after adding environment variables"
echo ""

echo "✅ Files Updated:"
echo "   - Backend/server.js (already using connectDB)"
echo "   - Backend/server_fixed_comprehensive.js"
echo "   - Backend/server_fixed_mobile.js"
echo "   - Backend/server_working.js"
echo "   - Backend/server_backup.js"
echo "   - Backend/server_backup_original.js"
echo "   - Backend/server_simple.js"
echo "   - Backend/config/database.js (improved error handling)"
echo ""

echo "🔗 MongoDB Atlas Setup Guide:"
echo "https://docs.mongodb.com/atlas/getting-started/"
echo ""

echo "🎯 Example MongoDB Atlas Connection String:"
echo "mongodb+srv://yourusername:yourpassword@cluster0.xxxxx.mongodb.net/sync_data?retryWrites=true&w=majority"
echo ""

read -p "Press Enter when you've configured the environment variables in Render..." 