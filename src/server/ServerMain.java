package server;

import controller.SystemController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main server class for the clothing store network management system.
 * Implements Thread-per-Client architecture - each client connection gets its own thread.
 * Listens on port 5000 and creates a new ClientHandler thread for each incoming connection.
 * 
 * @author FinalProject
 */
public class ServerMain {

    /** The port number the server listens on */
    private static final int PORT = 5000;

    /**
     * Main entry point for the server.
     * Creates a SystemController, starts listening on port 5000,
     * and creates a new thread for each client connection.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {

        SystemController controller = new SystemController();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, controller);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
