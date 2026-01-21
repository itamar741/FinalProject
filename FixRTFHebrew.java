import java.io.*;

public class FixRTFHebrew {
    
    public static void main(String[] args) {
        try {
            // Read the RTF file as UTF-8
            File file = new File("PROJECT_DOCUMENTATION.rtf");
            StringBuilder content = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            String text = content.toString();
            StringBuilder result = new StringBuilder();
            
            // Process character by character
            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);
                
                // Check if this is part of an RTF command (starts with \)
                if (c == '\\' && i + 1 < text.length()) {
                    char next = text.charAt(i + 1);
                    // If next char is part of RTF command, copy the whole command
                    if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z') ||
                        (next >= '0' && next <= '9') || next == '*' || next == '{' || 
                        next == '}' || next == '\\' || next == 'u') {
                        // This is an RTF command - find its end
                        int start = i;
                        i += 2; // Skip \ and first char
                        // RTF commands can have numbers after them
                        while (i < text.length() && 
                               ((text.charAt(i) >= '0' && text.charAt(i) <= '9') ||
                                text.charAt(i) == '-' || text.charAt(i) == ' ')) {
                            i++;
                        }
                        // Copy the RTF command as-is
                        result.append(text.substring(start, i));
                        continue;
                    }
                }
                
                // Check for RTF group markers
                if (c == '{' || c == '}') {
                    result.append(c);
                    i++;
                    continue;
                }
                
                // For regular text, escape Hebrew/Unicode
                if (c < 128) {
                    // ASCII - escape special RTF chars only
                    if (c == '\\') {
                        result.append("\\\\");
                    } else if (c == '{') {
                        result.append("\\{");
                    } else if (c == '}') {
                        result.append("\\}");
                    } else if (c == '\n') {
                        result.append("\\par\n");
                    } else {
                        result.append(c);
                    }
                } else {
                    // Unicode (Hebrew) - escape with \uXXXX?
                    result.append("\\u").append((int)c).append("?");
                }
                
                i++;
            }
            
            // Write the fixed file with Windows-1255 encoding
            try (FileWriter writer = new FileWriter("PROJECT_DOCUMENTATION.rtf", 
                    java.nio.charset.Charset.forName("Windows-1255"))) {
                writer.write(result.toString());
            }
            
            System.out.println("Fixed RTF file with Hebrew Unicode escapes");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
