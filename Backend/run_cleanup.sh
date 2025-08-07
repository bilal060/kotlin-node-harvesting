#!/bin/bash

echo "🧹 Starting duplicate cleanup process..."
echo "📅 $(date)"

# Run the cleanup script
node cleanup_duplicates.js

echo "✅ Cleanup process completed!"
echo "📅 $(date)" 