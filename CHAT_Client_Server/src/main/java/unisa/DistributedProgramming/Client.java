package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;


public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7777;
    private static boolean isLoggedIn = false;  // Variabile per gestire lo stato del login
    private static boolean messageConfirmed = false;  // Variabile per conferma di invio avvenuto
    private static final Object lock = new Object();


    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            // Thread per la ricezione dei messaggi dal server
            new Thread(() -> {

                String serverMessage;

                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);

                        // Controlla se è avvenuto il login
                        if (serverMessage.startsWith("Benvenuto")) {

                            synchronized (lock) {
                                isLoggedIn = true;  // Imposta il flag di login a true
                                lock.notify();      // Risveglia il thread principale
                            }

                        }

                        // Controlla se il server ha confermato l'invio del messaggio
                        else if (serverMessage.contains("Messaggio inviato") || serverMessage.contains("non è connesso")) {

                            synchronized (lock) {
                                messageConfirmed = true;  // Imposta il flag di conferma
                                lock.notify();  // Risveglia il thread principale per mostrare il menu
                            }

                        }
                        else if (serverMessage.startsWith("Login") || serverMessage.startsWith("Nome")) {
                            System.exit(1);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Errore nella ricezione dei messaggi: " + e.getMessage());
                }
            }).start();



            // Login
            System.out.print("Inserisci LOGIN <username>: ");
            String username = consoleReader.readLine();
            out.println("LOGIN " + username);

            // Attendi la risposta di login dal server
            synchronized (lock) {
                while (!isLoggedIn) {
                    lock.wait();  // Attendi che il thread di ricezione segnali il completamento del login
                }
            }


            // MENU'
            while (true) {

                System.out.println("\n--- Menu ---");
                System.out.println("Scegli un'opzione: ");
                System.out.println("1: Messaggio di Broadcast");
                System.out.println("2: Messaggio Uno-a-Uno");
                System.out.println("3: Logout");
                String choice = consoleReader.readLine();

                switch (choice) {
                    case "1":
                        // Messaggio di broadcast
                        System.out.print("Inserisci il tuo messaggio di broadcast: ");
                        String broadcastMessage = consoleReader.readLine();
                        out.println("BROADCAST " + broadcastMessage);

                        // Attendi conferma del server
                        synchronized (lock) {
                            messageConfirmed = false;  // Resetta il flag
                            while (!messageConfirmed) {
                                lock.wait();  // Attendi conferma di invio del messaggio
                            }
                        }
                        break;

                    case "2":
                        // Messaggio uno-a-uno
                        System.out.print("Inserisci il nome utente a cui inviare il messaggio: ");
                        String toUser = consoleReader.readLine();
                        System.out.print("Inserisci il tuo messaggio: ");
                        String oneToOneMessage = consoleReader.readLine();
                        out.println("ONE-TO-ONE " + toUser + " " + oneToOneMessage);

                        // Attendi conferma del server
                        synchronized (lock) {
                            messageConfirmed = false;  // Resetta il flag
                            while (!messageConfirmed) {
                                lock.wait();  // Attendi conferma di invio del messaggio
                            }
                        }
                        break;

                    case "3":
                        // Logout
                        out.println("LOGOUT " + username);
                        System.out.println("Disconnessione...");
                        return; // Termina il client

                    default:
                        System.out.println("Scelta non valida! Si prega di selezionare 1, 2 o 3...");
                        break;
                }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore nella connessione al server: " + e.getMessage());
        }
    }
}
