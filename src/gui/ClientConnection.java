package gui;

import java.io.*;
import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ClientConnection {
    
    private static final String DEFAULT_SERVER_HOST = "localhost"; // Fallback for local development
    private static final String CLIENT_CONFIG_FILE = "client.config";
    private static final int SERVER_PORT = 5000;
    
    private String serverHost;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    
    /**
     * Constructs a new ClientConnection.
     * Reads server address from client.config file if it exists,
     * otherwise uses default (localhost for development, or domain name if configured).
     */
    public ClientConnection() {
        this.serverHost = getServerHostFromConfig();
        System.out.println("ClientConnection initialized with server host: " + serverHost);
    }
    
    /**
     * Gets the server host address from configuration file or default.
     * 
     * @return the server host address
     */
    private String getServerHostFromConfig() {
        File configFile = new File(CLIENT_CONFIG_FILE);
        if (configFile.exists()) {
            try {
                Map<String, String> config = readConfigFile(configFile);
                String host = config.get("serverHost");
                if (host != null && !host.trim().isEmpty()) {
                    return host.trim();
                }
            } catch (IOException e) {
                System.err.println("Error reading client.config: " + e.getMessage());
            }
        }
        
        // Default: try to use a common domain name pattern
        // Users should create client.config with their server's domain name
        // For now, return localhost as fallback
        return DEFAULT_SERVER_HOST;
    }
    
    /**
     * Reads configuration file and returns key-value pairs.
     * 
     * @param configFile the configuration file
     * @return map of configuration key-value pairs
     * @throws IOException if file cannot be read
     */
    private Map<String, String> readConfigFile(File configFile) throws IOException {
        Map<String, String> config = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse key=value format
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    config.put(key, value);
                }
            }
        }
        
        return config;
    }
    
    /**
     * התחברות לשרת
     */
    public boolean connect() {
        try {
            System.out.println("Attempting to connect to server at " + serverHost + ":" + SERVER_PORT);
            socket = new Socket(serverHost, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String response = in.readLine();
            if (response != null && response.equals("CONNECTED")) {
                connected = true;
                System.out.println("Successfully connected to server");
                return true;
            }
            
            disconnect();
            return false;
        } catch (IOException e) {
            System.err.println("Connection error to " + serverHost + ":" + SERVER_PORT + " - " + e.getMessage());
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
