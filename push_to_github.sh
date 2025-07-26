#!/bin/bash

echo "🚀 Pushing all code to GitHub repository..."

# Initialize git if not already done
if [ ! -d ".git" ]; then
    echo "📁 Initializing git repository..."
    git init
fi

# Set remote origin
echo "🔗 Setting remote origin..."
git remote remove origin 2>/dev/null
git remote add origin git@github.com:bilal060/kotlin-node-harvesting.git

# Add all files
echo "📦 Adding all files..."
git add .

# Commit with descriptive message
echo "💾 Committing changes..."
git commit -m "Complete DeviceSync System: Kotlin Mobile App + Node.js Backend

- Kotlin Android app with data synchronization
- Node.js backend with MongoDB
- API endpoints for device registration and data sync
- Support for Contacts, Call Logs, Messages, Notifications
- Comprehensive testing scripts
- Documentation and setup guides"

# Push to main branch
echo "⬆️ Pushing to GitHub..."
git branch -M main
git push -u origin main

echo "✅ Code successfully pushed to GitHub!"
echo "🌐 Repository: https://github.com/bilal060/kotlin-node-harvesting" 