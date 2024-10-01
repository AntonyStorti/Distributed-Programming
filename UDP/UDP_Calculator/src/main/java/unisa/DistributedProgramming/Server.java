package unisa.DistributedProgramming;
import java.net.*;

public class Server {

    public static void main(String[] args) {

        DatagramSocket socket = null;

        try {

            socket = new DatagramSocket(9876);
            System.out.println("Il server è in esecuzione...");

            while (true) {

                // Ricevere la richiesta:
                byte[] requestData = new byte[1024];
                DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length);
                socket.receive(requestPacket);

                // Elabora la richiesta:
                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Richiesta ricevuta: " + request);
                String[] parts = request.split(" ");
                String operation = parts[0];

                double result = 0;

                switch (operation) {
                    case "sqrt":
                        result = Math.sqrt(Double.parseDouble(parts[1]));
                        break;
                    case "pow":
                        result = Math.pow(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                        break;
                    case "max":
                        result = Math.max(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                        for (int i = 3; i < parts.length; i++) {
                            result = Math.max(result, Double.parseDouble(parts[i]));
                        }
                        break;
                    case "min":
                        result = Math.min(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                        for (int i = 3; i < parts.length; i++) {
                            result = Math.min(result, Double.parseDouble(parts[i]));
                        }
                        break;
                    case "avg":
                        double sum = 0;
                        for (int i = 1; i < parts.length; i++) {
                            sum += Double.parseDouble(parts[i]);
                        }
                        result = sum / (parts.length - 1);
                        break;
                    case "+":
                        for (int i = 1; i < parts.length; i++) {
                            result += Double.parseDouble(parts[i]);
                        }
                        break;
                    case "-":
                        result = Double.parseDouble(parts[1]);
                        for (int i = 2; i < parts.length; i++) {
                            result -= Double.parseDouble(parts[i]);
                        }
                        break;
                    case "*":
                        result = 1;
                        for (int i = 1; i < parts.length; i++) {
                            result *= Double.parseDouble(parts[i]);
                        }
                        break;
                    case "/":
                        result = Double.parseDouble(parts[1]);
                        for (int i = 2; i < parts.length; i++) {
                            result /= Double.parseDouble(parts[i]);
                        }
                        break;
                    default:
                        System.out.println("Operazione sconosciuta: " + operation);
                        break;
                }

                // Prepara la risposta:
                String response = String.valueOf(result);
                byte[] responseData = response.getBytes();
                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();

                // Invia la risposta al client:
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                socket.send(responsePacket);
                System.out.println("Risposta inviata: " + response);

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
