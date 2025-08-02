#!/usr/bin/env python3
"""
Device Code Update Script
This script allows admins to easily update the device code in the mobile app configuration.
"""

import json
import os
import sys
import re

def update_device_code(new_code):
    """
    Update the device code in the configuration file.
    
    Args:
        new_code (str): The new 5-digit device code
    """
    
    # Validate the device code
    if not re.match(r'^\d{5}$', new_code):
        print("❌ Error: Device code must be exactly 5 digits")
        return False
    
    config_file = "app/src/main/assets/device_config.json"
    
    # Check if config file exists
    if not os.path.exists(config_file):
        print(f"❌ Error: Configuration file not found at {config_file}")
        return False
    
    try:
        # Read current configuration
        with open(config_file, 'r') as f:
            config = json.load(f)
        
        # Update device code
        old_code = config.get('deviceCode', 'Unknown')
        config['deviceCode'] = new_code
        
        # Write updated configuration
        with open(config_file, 'w') as f:
            json.dump(config, f, indent=2)
        
        print(f"✅ Device code updated successfully!")
        print(f"   Old code: {old_code}")
        print(f"   New code: {new_code}")
        print(f"   File: {config_file}")
        
        return True
        
    except json.JSONDecodeError as e:
        print(f"❌ Error: Invalid JSON in configuration file: {e}")
        return False
    except Exception as e:
        print(f"❌ Error updating device code: {e}")
        return False

def show_current_config():
    """
    Display the current configuration.
    """
    config_file = "app/src/main/assets/device_config.json"
    
    if not os.path.exists(config_file):
        print("❌ Configuration file not found")
        return
    
    try:
        with open(config_file, 'r') as f:
            config = json.load(f)
        
        print("📱 Current Device Configuration:")
        print(f"   Device Code: {config.get('deviceCode', 'Not set')}")
        print(f"   App Version: {config.get('appVersion', 'Not set')}")
        print(f"   Sync Interval: {config.get('syncInterval', 'Not set')}ms")
        print(f"   Max Retries: {config.get('maxRetries', 'Not set')}")
        print(f"   Enabled Data Types: {', '.join(config.get('enabledDataTypes', []))}")
        
    except Exception as e:
        print(f"❌ Error reading configuration: {e}")

def main():
    """
    Main function to handle command line arguments.
    """
    if len(sys.argv) < 2:
        print("🔧 Device Code Update Tool")
        print("Usage:")
        print("  python update_device_code.py <new_5_digit_code>")
        print("  python update_device_code.py --show")
        print("")
        print("Examples:")
        print("  python update_device_code.py 12345")
        print("  python update_device_code.py --show")
        return
    
    if sys.argv[1] == "--show":
        show_current_config()
        return
    
    new_code = sys.argv[1]
    
    if update_device_code(new_code):
        print("\n📋 Next Steps:")
        print("1. Build the APK with the new device code")
        print("2. Install the APK on the target device")
        print("3. The app will automatically use the new device code")
        print("\n💡 Tip: You can verify the device code in the app logs")

if __name__ == "__main__":
    main() 