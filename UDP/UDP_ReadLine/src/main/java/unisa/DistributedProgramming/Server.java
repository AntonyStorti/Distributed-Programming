package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {

        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server in esecuzione... ");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                // Gestisci i vari Client
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
