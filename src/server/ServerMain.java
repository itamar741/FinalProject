package server;

import controller.SystemController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 5000;

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
