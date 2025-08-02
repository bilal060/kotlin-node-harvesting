#!/usr/bin/env python3
"""
Script to scan all XML layout files and identify text that needs translation.
This will help us migrate from @string/ references to DynamicStringManager.
"""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

def scan_layout_files(layout_dir):
    """Scan all layout files and extract text that needs translation."""
    
    # Patterns to match different types of text in XML
    patterns = {
        'string_reference': r'@string/(\w+)',
        'hardcoded_text': r'android:text="([^"]+)"',
        'content_description': r'android:contentDescription="([^"]+)"',
        'hint_text': r'android:hint="([^"]+)"',
        'title_text': r'android:title="([^"]+)"'
    }
    
    all_texts = {
        'string_references': set(),
        'hardcoded_texts': set(),
        'content_descriptions': set(),
        'hint_texts': set(),
        'title_texts': set()
    }
    
    layout_path = Path(layout_dir)
    
    for xml_file in layout_path.glob('*.xml'):
        print(f"Scanning: {xml_file.name}")
        
        try:
            with open(xml_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Find string references
            string_refs = re.findall(patterns['string_reference'], content)
            all_texts['string_references'].update(string_refs)
            
            # Find hardcoded text
            hardcoded = re.findall(patterns['hardcoded_text'], content)
            all_texts['hardcoded_texts'].update(hardcoded)
            
            # Find content descriptions
            content_desc = re.findall(patterns['content_description'], content)
            all_texts['content_descriptions'].update(content_desc)
            
            # Find hint texts
            hints = re.findall(patterns['hint_text'], content)
            all_texts['hint_texts'].update(hints)
            
            # Find title texts
            titles = re.findall(patterns['title_text'], content)
            all_texts['title_texts'].update(titles)
            
        except Exception as e:
            print(f"Error reading {xml_file}: {e}")
    
    return all_texts

def generate_translation_mapping(texts):
    """Generate a mapping of string references to DynamicStringManager keys."""
    
    mapping = {}
    
    # Process string references
    for string_ref in texts['string_references']:
        # Convert camelCase or snake_case to a more readable format
        key = string_ref.lower().replace('_', ' ')
        mapping[f"@{string_ref}"] = key
    
    # Process hardcoded texts (filter out empty and very short texts)
    for text in texts['hardcoded_texts']:
        if len(text.strip()) > 2 and not text.startswith('@'):
            # Create a key from the text
            key = text.lower().replace(' ', '_').replace('-', '_').replace(':', '').replace('.', '')
            key = re.sub(r'[^\w\s]', '', key)
            mapping[text] = key
    
    return mapping

def generate_dynamic_string_manager_update(texts, mapping):
    """Generate the updated DynamicStringManager content."""
    
    template = """// Auto-generated from layout files
// Add these to DynamicStringManager.kt in the appStrings map

"""
    
    # Add string references
    if texts['string_references']:
        template += "// String references from layouts:\n"
        for string_ref in sorted(texts['string_references']):
            key = mapping.get(f"@{string_ref}", string_ref.lower().replace('_', ' '))
            template += f'"{key}" to "TODO: Add translation for {string_ref}",\n'
        template += "\n"
    
    # Add hardcoded texts
    if texts['hardcoded_texts']:
        template += "// Hardcoded texts from layouts:\n"
        for text in sorted(texts['hardcoded_texts']):
            if len(text.strip()) > 2 and not text.startswith('@'):
                key = mapping.get(text, text.lower().replace(' ', '_').replace('-', '_'))
                template += f'"{key}" to "{text}",\n'
        template += "\n"
    
    return template

def main():
    layout_dir = "app/src/main/res/layout"
    
    print("🔍 Scanning layout files for translation needs...")
    texts = scan_layout_files(layout_dir)
    
    print("\n📊 Found the following text types:")
    print(f"  • String references: {len(texts['string_references'])}")
    print(f"  • Hardcoded texts: {len(texts['hardcoded_texts'])}")
    print(f"  • Content descriptions: {len(texts['content_descriptions'])}")
    print(f"  • Hint texts: {len(texts['hint_texts'])}")
    print(f"  • Title texts: {len(texts['title_texts'])}")
    
    print("\n📝 String references found:")
    for ref in sorted(texts['string_references']):
        print(f"  • @string/{ref}")
    
    print("\n📝 Hardcoded texts found:")
    for text in sorted(texts['hardcoded_texts']):
        if len(text.strip()) > 2 and not text.startswith('@'):
            print(f"  • \"{text}\"")
    
    # Generate mapping
    mapping = generate_translation_mapping(texts)
    
    # Generate update content
    update_content = generate_dynamic_string_manager_update(texts, mapping)
    
    # Write to file
    with open('layout_translation_mapping.txt', 'w', encoding='utf-8') as f:
        f.write(update_content)
    
    print(f"\n✅ Generated mapping file: layout_translation_mapping.txt")
    print("📋 Use this to update DynamicStringManager.kt")

if __name__ == "__main__":
    main()