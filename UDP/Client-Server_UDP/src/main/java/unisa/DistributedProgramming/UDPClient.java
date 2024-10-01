package unisa.DistributedProgramming;
import java.net.*;
import java.io.*;

public class UDPClient {

    public static void main(String[] args) {

        DatagramSocket socket = null;
        BufferedReader userInput = null;

        try {

            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 9876;

            // Imposta un timeout di 5 secondi (5000 millisecondi)
            socket.setSoTimeout(5000);

            // Crea un BufferedReader per leggere l'input dall'utente:
            userInput = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Inserisci il messaggio da inviare (scrivi 'exit' per uscire): ");
                String message = userInput.readLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Invia il messaggio al server
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                socket.send(sendPacket);

                // Ricevi la risposta dal server:
                try {

                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Risposta dal server:\n" + receivedMessage);

                } catch (SocketTimeoutException e) {
                    System.out.println("Nessuna risposta dal server, timeout scaduto.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            try {
                if (userInput != null) {
                    userInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
