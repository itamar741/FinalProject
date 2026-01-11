package gui;

import java.io.*;
import java.net.Socket;

/**
 * מחלקה לתקשורת עם השרת
 */
public class ClientConnection {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    
    /**
     * התחברות לשרת
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // קריאת הודעת CONNECTED מהשרת
            String response = in.readLine();
            if (response != null && response.equals("CONNECTED")) {
                connected = true;
                return true;
            }
            
            disconnect();
            return false;
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * התנתקות מהשרת
     */
    public void disconnect() {
        connected = false;
        try {
            if (out != null) {
                out.println("EXIT");
                out.flush();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Disconnect error: " + e.getMessage());
        }
    }
    
    /**
     * שליחת פקודה לשרת וקבלת תגובה
     */
    public String sendCommand(String command) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }
        
        try {
            out.println(command);
            String response = in.readLine();
            
            if (response == null) {
                throw new IOException("Server disconnected");
            }
            
            return response;
        } catch (IOException e) {
            connected = false;
            throw new IOException("Communication error: " + e.getMessage());
        }
    }
    
    /**
     * התחברות עם שם משתמש וסיסמה
     */
    public String login(String username, String password) throws IOException {
        String command = "LOGIN;" + username + ";" + password;
        return sendCommand(command);
    }
    
    /**
     * התנתקות
     */
    public String logout() throws IOException {
        return sendCommand("LOGOUT");
    }
    
    /**
     * בדיקה אם מחובר לשרת
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
