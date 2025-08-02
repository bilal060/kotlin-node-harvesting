#!/usr/bin/env python3
"""
Script to update the backend URL in app_config.json
Usage: python update_backend_url.py <new_backend_url>
"""

import json
import sys
import os

def update_backend_url(new_url):
    """Update the backend URL in app_config.json"""
    
    config_file = "app/src/main/assets/app_config.json"
    
    if not os.path.exists(config_file):
        print(f"❌ Error: {config_file} not found")
        return False
    
    try:
        # Read current configuration
        with open(config_file, 'r') as f:
            config = json.load(f)
        
        # Update backend URL
        old_url = config.get('backend_url', 'http://localhost:5002')
        config['backend_url'] = new_url
        
        # Write updated configuration
        with open(config_file, 'w') as f:
            json.dump(config, f, indent=2)
        
        print(f"✅ Backend URL updated successfully!")
        print(f"   Old URL: {old_url}")
        print(f"   New URL: {new_url}")
        print(f"   Config file: {config_file}")
        
        return True
        
    except Exception as e:
        print(f"❌ Error updating backend URL: {e}")
        return False

def main():
    if len(sys.argv) != 2:
        print("Usage: python update_backend_url.py <new_backend_url>")
        print("Example: python update_backend_url.py https://your-backend.com")
        sys.exit(1)
    
    new_url = sys.argv[1]
    
    # Validate URL format
    if not new_url.startswith(('http://', 'https://')):
        print("❌ Error: URL must start with http:// or https://")
        sys.exit(1)
    
    if update_backend_url(new_url):
        print("\n📱 Next steps:")
        print("1. Build the app: ./gradlew assembleDebug")
        print("2. Install the app: adb install -r app/build/outputs/apk/debug/app-debug.apk")
        print("3. The app will now use the new backend URL for data transmission")
    else:
        sys.exit(1)

if __name__ == "__main__":
    main() 