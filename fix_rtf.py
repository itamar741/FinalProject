#!/usr/bin/env python3
# -*- coding: utf-8 -*-

def escape_rtf_unicode(text):
    """Convert text to RTF with Unicode escapes for non-ASCII characters"""
    result = []
    i = 0
    while i < len(text):
        c = text[i]
        
        # Check if this is part of an RTF command (starts with \)
        if c == '\\' and i + 1 < len(text):
            next_char = text[i + 1]
            # If next char is part of RTF command, copy the whole command
            if (next_char.isalpha() or next_char.isdigit() or 
                next_char in ['*', '{', '}', '\\', 'u', '-', ' ']):
                # This is an RTF command - find its end
                start = i
                i += 2  # Skip \ and first char
                # RTF commands can have numbers after them
                while i < len(text):
                    char = text[i]
                    if char.isdigit() or char in ['-', ' ']:
                        i += 1
                    else:
                        break
                # Copy the RTF command as-is
                result.append(text[start:i])
                continue
        
        # Check for RTF group markers
        if c == '{' or c == '}':
            result.append(c)
            i += 1
            continue
        
        # For regular text, escape Hebrew/Unicode
        if ord(c) < 128:
            # ASCII - escape special RTF chars only
            if c == '\\':
                result.append('\\\\')
            elif c == '{':
                result.append('\\{')
            elif c == '}':
                result.append('\\}')
            elif c == '\n':
                result.append('\\par\n')
            else:
                result.append(c)
        else:
            # Unicode (Hebrew) - escape with \uXXXX?
            result.append('\\u{}?'.format(ord(c)))
        
        i += 1
    
    return ''.join(result)

# Read the RTF file
with open('PROJECT_DOCUMENTATION.rtf', 'r', encoding='utf-8') as f:
    content = f.read()

# Convert Hebrew to Unicode escapes
fixed_content = escape_rtf_unicode(content)

# Write the fixed file to a temp file first
with open('PROJECT_DOCUMENTATION_FIXED.rtf', 'w', encoding='utf-8') as f:
    f.write(fixed_content)

print("Fixed RTF file created as PROJECT_DOCUMENTATION_FIXED.rtf")
print("Please close PROJECT_DOCUMENTATION.rtf and then rename the fixed file")
