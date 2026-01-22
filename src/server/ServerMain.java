package server;

import controller.SystemController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Main server class for the clothing store network management system.
 * Implements Thread-per-Client architecture - each client connection gets its own thread.
 * Listens on port 5000 and creates a new ClientHandler thread for each incoming connection.
 * Maintains a Vector<Socket> to track all connected clients for Broadcast functionality.
 * 
 * @author FinalProject
 */
public class ServerMain {

    /** The port number the server listens on */
    private static final int PORT = 5000;
    
    /** Vector to store all connected client sockets (thread-safe) */
    private static Vector<Socket> connectedClients = new Vector<>();

    /**
     * Main entry point for the server.
     * Creates a SystemController, starts listening on port 5000,
     * and creates a new thread for each client connection.
     * Each client socket is added to the Vector<Socket> for Broadcast support.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {

        SystemController controller = new SystemController();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            System.out.println("Server is ready to accept connections from any network interface");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // Add socket to Vector (thread-safe)
                connectedClients.add(clientSocket);
                
                // Create handler and start thread
                ClientHandler handler = new ClientHandler(clientSocket, controller, connectedClients);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
