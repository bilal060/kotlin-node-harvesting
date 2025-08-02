#!/usr/bin/env python3
import os
import re
import glob

def remove_border_radius_from_file(file_path):
    """Remove all border radius attributes from an XML file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Remove cornerRadius attributes
        content = re.sub(r'app:cornerRadius="[^"]*"', '', content)
        content = re.sub(r'android:cornerRadius="[^"]*"', '', content)
        
        # Remove cardCornerRadius attributes
        content = re.sub(r'app:cardCornerRadius="[^"]*"', '', content)
        content = re.sub(r'android:cardCornerRadius="[^"]*"', '', content)
        
        # Remove boxCornerRadius attributes
        content = re.sub(r'app:boxCornerRadiusBottomEnd="[^"]*"', '', content)
        content = re.sub(r'app:boxCornerRadiusBottomStart="[^"]*"', '', content)
        content = re.sub(r'app:boxCornerRadiusTopEnd="[^"]*"', '', content)
        content = re.sub(r'app:boxCornerRadiusTopStart="[^"]*"', '', content)
        
        # Remove corners radius in drawable shapes
        content = re.sub(r'<corners android:radius="[^"]*" />', '<corners android:radius="0dp" />', content)
        
        # Remove cornerRadius from theme styles
        content = re.sub(r'<item name="cornerRadius">[^<]*</item>', '<item name="cornerRadius">0dp</item>', content)
        content = re.sub(r'<item name="cardCornerRadius">[^<]*</item>', '<item name="cardCornerRadius">0dp</item>', content)
        
        # Write back if content changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ Updated: {file_path}")
            return True
        else:
            print(f"⏭️  No changes: {file_path}")
            return False
            
    except Exception as e:
        print(f"❌ Error processing {file_path}: {e}")
        return False

def main():
    """Main function to process all XML files."""
    print("🔧 Removing border radius from all XML files...")
    print("=" * 60)
    
    # Get all XML files in the res directory
    xml_files = []
    
    # Layout files
    layout_files = glob.glob("app/src/main/res/layout/*.xml")
    xml_files.extend(layout_files)
    
    # Drawable files
    drawable_files = glob.glob("app/src/main/res/drawable/*.xml")
    xml_files.extend(drawable_files)
    
    # Values files
    values_files = glob.glob("app/src/main/res/values/*.xml")
    xml_files.extend(values_files)
    
    # Values-night files
    values_night_files = glob.glob("app/src/main/res/values-night/*.xml")
    xml_files.extend(values_night_files)
    
    total_files = len(xml_files)
    updated_files = 0
    
    print(f"📁 Found {total_files} XML files to process")
    print()
    
    for file_path in xml_files:
        if remove_border_radius_from_file(file_path):
            updated_files += 1
    
    print()
    print("=" * 60)
    print(f"📊 Summary:")
    print(f"   Total files processed: {total_files}")
    print(f"   Files updated: {updated_files}")
    print(f"   Files unchanged: {total_files - updated_files}")
    print()
    print("✅ Border radius removal complete!")

if __name__ == "__main__":
    main() 