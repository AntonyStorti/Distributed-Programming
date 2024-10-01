package unisa.DistributedProgramming;
import java.io.*;
import java.net.*;

public class Client {

    private static final int PORT = 12345;
    private static final int LINES_TO_RECEIVE = 20; // Numero di linee da leggere

    public static void main(String[] args) {

        String multicastAddress = "230.0.0.1";

        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {

            InetAddress group = InetAddress.getByName(multicastAddress);

            // PERCHE' E' DEPRECATO ???
            multicastSocket.joinGroup(group);

            System.out.println("Joined multicast group: " + multicastAddress);
            int linesReceived = 0;

            while (linesReceived < LINES_TO_RECEIVE) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                String line = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Ricevo: " + line);
                linesReceived++;
            }

            // PERCHE' E' DEPRECATO ???
            multicastSocket.leaveGroup(group);
            System.out.println("Lascia il gruppo: " + multicastAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
