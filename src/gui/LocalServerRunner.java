package gui;

import controller.SystemController;
import server.ClientHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Runs a local server in a separate thread.
 * Used when remote server is not available.
 */
public class LocalServerRunner extends Thread {
    
    private static LocalServerRunner instance;
    private static final int PORT = 5000;
    private SystemController controller;
    private ServerSocket serverSocket;
    private Vector<Socket> connectedClients;
    private boolean running = false;
    
    /**
     * Gets or creates the singleton instance of LocalServerRunner.
     * 
     * @return the LocalServerRunner instance
     */
    public static synchronized LocalServerRunner getInstance() {
        if (instance == null) {
            instance = new LocalServerRunner();
        }
        return instance;
    }
    
    /**
     * Checks if local server is already running.
     * 
     * @return true if server is running, false otherwise
     */
    public static boolean isServerRunning() {
        try {
            Socket testSocket = new Socket("localhost", PORT);
            testSocket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private LocalServerRunner() {
        setDaemon(true);
        connectedClients = new Vector<>();
    }
    
    @Override
    public void run() {
        try {
            controller = new SystemController();
            serverSocket = new ServerSocket(PORT);
            running = true;
            
            System.out.println("Local server started on port " + PORT);
            
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    connectedClients.add(clientSocket);
                    
                    ClientHandler handler = new ClientHandler(
                        clientSocket, 
                        controller, 
                        connectedClients
                    );
                    new Thread(handler).start();
                } catch (java.net.SocketException e) {
                    // Socket was closed, this is expected when stopping
                    if (running) {
                        System.err.println("Socket closed: " + e.getMessage());
                    }
                    break;
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (java.net.BindException e) {
            System.err.println("Port " + PORT + " is already in use. Another server may be running.");
            running = false;
        } catch (Exception e) {
            System.err.println("Failed to start local server: " + e.getMessage());
            running = false;
        }
    }
    
    /**
     * Stops the local server.
     */
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (controller != null) {
                controller.saveAllData();
            }
        } catch (Exception e) {
            System.err.println("Error stopping local server: " + e.getMessage());
        }
    }
    
    /**
     * Checks if the server is currently running.
     * 
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
