package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Ottieni filename e numero riga dall'utente:
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Inserisci il filename (con estensione): ");
            String filename = userInput.readLine();
            System.out.print("Inserisci il numero di riga: ");
            int lineNumber = Integer.parseInt(userInput.readLine());

            // Inviali al Server:
            out.println(filename);
            out.println(lineNumber);

            // Ricevi la risposta
            String response = in.readLine();
            System.out.println("Risposta dal Server: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Invalid line number format");
        }
    }
}
