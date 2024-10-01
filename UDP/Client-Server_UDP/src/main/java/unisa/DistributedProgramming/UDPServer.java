package unisa.DistributedProgramming;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class UDPServer {

    private static final int PORT = 9876;
    private static final Queue<String> messageHistory = new LinkedList<>();

    public static void main(String[] args) {

        DatagramSocket socket = null;

        try {

            socket = new DatagramSocket(PORT);
            System.out.println("Server in esecuzione...");

            while (true) {

                // Buffer per ricevere i dati in arrivo
                byte[] receiveData = new byte[1024];

                // Riceve i dati in arrivo dal client:
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                System.out.println("Messaggio ricevuto dal client: " + clientMessage);

                // Memorizza il messaggio ricevuto nella coda:
                if (messageHistory.size() == 2) {
                    messageHistory.poll(); // Rimuove il messaggio più vecchio se la dimensione supera 2
                }
                messageHistory.add(clientMessage);


                StringBuilder responseMessage = new StringBuilder();

                if (messageHistory.size() == 1) {
                    // Se c'è solo un messaggio, invialo una volta
                    responseMessage.append(messageHistory.peek());
                } else {
                    // Se ci sono due messaggi, inviali entrambi
                    for (String msg : messageHistory) {
                        responseMessage.append(msg).append("\n");
                    }
                }

                // Invia il messaggio di risposta al client
                byte[] sendData = responseMessage.toString().trim().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(sendPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
