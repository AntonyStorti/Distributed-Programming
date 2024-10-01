package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;

public class Server {

    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 12345;
    private static final String FILENAME = "sample.txt"; // File da cui leggere

    public static void main(String[] args) {

        try (MulticastSocket multicastSocket = new MulticastSocket()) {

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            BufferedReader fileReader = new BufferedReader(new FileReader(FILENAME));

            String line;
            while (true) {

                // Invia in loop le linee del file (ognuna ogni 2 secondi)
                while ((line = fileReader.readLine()) != null) {
                    byte[] buffer = line.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                    multicastSocket.send(packet);
                    System.out.println("Invio: " + line);
                    Thread.sleep(2000); // Aspetta per inviare la prossima linea
                }

                // Quando finisce il file --> riparti dall'inizio
                fileReader.close();
                fileReader = new BufferedReader(new FileReader(FILENAME));
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
