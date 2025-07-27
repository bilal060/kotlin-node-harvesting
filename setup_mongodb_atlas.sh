c#!/bin/bash

echo "🚀 Setting up MongoDB Atlas for Production Deployment"
echo "=================================================="

echo ""
echo "📋 Steps to set up MongoDB Atlas:"
echo "1. Go to https://www.mongodb.com/atlas"
echo "2. Create a free account"
echo "3. Create a new cluster (M0 Free tier)"
echo "4. Set up database access (create username/password)"
echo "5. Set up network access (allow all IPs: 0.0.0.0/0)"
echo "6. Get your connection string"
echo ""

echo "🔧 Once you have your MongoDB Atlas connection string:"
echo "1. Go to your Render dashboard"
echo "2. Select your kotlin-node-harvesting service"
echo "3. Go to Environment tab"
echo "4. Add environment variable:"
echo "   - Key: MONGODB_URI"
echo "   - Value: mongodb+srv://username:password@cluster.mongodb.net/sync_data?retryWrites=true&w=majority"
echo "5. Replace username, password, and cluster with your actual values"
echo "6. Save and redeploy"
echo ""

echo "📝 Example MongoDB Atlas connection string format:"
echo "mongodb+srv://yourusername:yourpassword@cluster0.xxxxx.mongodb.net/sync_data?retryWrites=true&w=majority"
echo ""

echo "✅ After setting up MongoDB Atlas:"
echo "- Your backend will connect to the cloud database"
echo "- Data will persist across deployments"
echo "- No more localhost connection errors"
echo ""

echo "🔗 Quick MongoDB Atlas Setup Guide:"
echo "https://docs.mongodb.com/atlas/getting-started/"
echo ""

echo "🎯 Alternative: Use MongoDB Atlas Free Tier"
echo "- 512MB storage"
echo "- Shared clusters"
echo "- Perfect for development and small production apps"
echo ""

read -p "Press Enter when you've set up MongoDB Atlas and added the MONGODB_URI to Render..." 