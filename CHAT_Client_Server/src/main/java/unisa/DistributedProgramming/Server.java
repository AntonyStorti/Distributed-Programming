package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;
import java.util.*;


public class Server {

    private static final int PORT = 7777;
    private static Map<String, ClientHandler> clients = new HashMap<>();
    private static final Object lock = new Object();

    public static void main(String[] args) {

        System.out.println("Il Server è in esecuzione...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Metodo per inviare un messaggio di Broadcast
    public static void broadcastMessage(String message, String fromUser) {

        synchronized (lock) {
            for (ClientHandler client : clients.values()) {
                if (!client.getUsername().equals(fromUser)) {
                    client.sendMessage("BROADCAST da " + fromUser + ": " + message);
                    clients.get(fromUser).sendMessage("Messaggio inviato correttamente.");
                }
            }
        }

    }


    // Metodo per inviare un messaggio a un singolo utente
    public static void sendOneToOneMessage(String message, String toUser, String fromUser) {

        synchronized (lock) {
            ClientHandler client = clients.get(toUser);
            if (client != null) {
                client.sendMessage("ONETOONE da " + fromUser + ": " + message);
                clients.get(fromUser).sendMessage("Messaggio inviato correttamente a " + toUser);
            } else {
                clients.get(fromUser).sendMessage("L'utente " + toUser + " non è connesso!");
            }
        }

    }


    // Metodo per gestire l'aggiunta di un utente
    public static boolean addClient(String username, ClientHandler clientHandler) {

        synchronized (lock) {
            if (clients.containsKey(username)) {
                return false;
            } else {
                clients.put(username, clientHandler);
                return true;
            }
        }

    }


    // Metodo per rimuovere un utente
    public static void removeClient(String username) {

        synchronized (lock) {
            clients.remove(username);
        }

    }
}
