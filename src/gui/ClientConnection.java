package gui;

import java.io.*;
import java.net.Socket;

/**
 * מחלקה לתקשורת עם השרת
 */
public class ClientConnection {
    
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    private String serverHost;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    
    /**
     * Constructs a new ClientConnection with default server host (localhost).
     */
    public ClientConnection() {
        this.serverHost = DEFAULT_SERVER_HOST;
    }
    
    /**
     * Constructs a new ClientConnection with specified server host.
     * 
     * @param serverHost the server host address (IP or hostname)
     */
    public ClientConnection(String serverHost) {
        this.serverHost = (serverHost != null && !serverHost.trim().isEmpty()) 
            ? serverHost.trim() 
            : DEFAULT_SERVER_HOST;
    }
    
    /**
     * התחברות לשרת
     */
    public boolean connect() {
        try {
            socket = new Socket(serverHost, SERVER_PORT);
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
     * Disconnects from the server.
     * Sends EXIT command and closes all streams and socket.
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
     * Sends a command to the server and receives a response.
     * 
     * @param command the command string to send (format: "COMMAND;param1;param2;...")
     * @return the response string from the server
     * @throws IOException if not connected, server disconnected, or communication error
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
     * Logs in with username and password.
     * 
     * @param username the username
     * @param password the password
     * @return the server response (format: "LOGIN_SUCCESS;role;branchId" or "AUTH_ERROR;...")
     * @throws IOException if communication error occurs
     */
    public String login(String username, String password) throws IOException {
        String command = "LOGIN;" + username + ";" + password;
        return sendCommand(command);
    }
    
    /**
     * Logs out from the server.
     * 
     * @return the server response
     * @throws IOException if communication error occurs
     */
    public String logout() throws IOException {
        return sendCommand("LOGOUT");
    }
    
    /**
     * Checks if connected to the server.
     * 
     * @return true if connected and socket is open, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
