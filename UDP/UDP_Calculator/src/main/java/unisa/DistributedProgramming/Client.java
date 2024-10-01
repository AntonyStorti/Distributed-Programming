package unisa.DistributedProgramming;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        DatagramSocket socket = null;

        try {

            socket = new DatagramSocket();

            // Ottieni l'input dell'utente:
            Scanner scanner = new Scanner(System.in);
            System.out.println("Inserisci un'operazione matematica (sqrt, pow, max, min, avg, +, -, *, /) seguita da numeri:");
            String userInput = scanner.nextLine();

            byte[] requestData = userInput.getBytes();

            // Crea un DatagramPacket per inviare la richiesta:
            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress, 9876);

            // Invia la richiesta al server:
            socket.send(requestPacket);
            System.out.println("Richiesta inviata: " + userInput);

            // Risposta:
            byte[] responseData = new byte[1024]; // Buffer per la risposta
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
            socket.receive(responsePacket);

            // Converti la risposta decimale in una stringa:
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Risposta dal server: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
