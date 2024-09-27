package unisa.DistributedProgramming;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {

        try {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Gestione del login
            String loginMessage = in.readLine();
            if (!loginMessage.startsWith("LOGIN ")) {
                out.println("Login non valido!");
                socket.close();
                return;
            }

            username = loginMessage.split(" ")[1];
            if (!Server.addClient(username, this)) {
                out.println("Nome utente già in uso!");
                socket.close();
                return;
            }

            out.println("Benvenuto nella chat, " + username + "!");
            System.out.println(username + " si è unito.");

            String message;

            while ((message = in.readLine()) != null) {
                if (message.startsWith("BROADCAST ")) {
                    String broadcastMessage = message.substring(10);
                    Server.broadcastMessage(broadcastMessage, username);
                } else if (message.startsWith("ONE-TO-ONE ")) {
                    String[] splitMessage = message.split(" ", 3);
                    String toUser = splitMessage[1];
                    String oneToOneMessage = splitMessage[2];
                    Server.sendOneToOneMessage(oneToOneMessage, toUser, username);
                } else if (message.startsWith("LOGOUT ")) {
                    String logoutUser = message.split(" ")[1];
                    if (logoutUser.equals(username)) {
                        break;
                    }
                } else {
                    out.println("Comando sconosciuto.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Server.removeClient(username);
                System.out.println(username + " ha lasciato.");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

}
