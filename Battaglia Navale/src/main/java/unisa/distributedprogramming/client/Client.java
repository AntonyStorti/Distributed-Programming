package unisa.distributedprogramming.client;

import unisa.distributedprogramming.Communicator;
import unisa.distributedprogramming.message.Message;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javafx.beans.property.BooleanProperty;


public class Client extends Communicator implements Runnable{

    private static DatagramSocket sock = null;
    private static InetAddress IPAddress = null;
    private static int port = -1;

    private static String username = null;

    private static BooleanProperty waiting;
    private static BooleanProperty placing;
    private static BooleanProperty playing;

    private static int hits = 0;
    private static int totHits = 0;

    private boolean finished = false;

    /**
     * Costruttore usato dal thread.
     */
    public Client() {

    }

    /**
     * Costruttore usato dal main.
     * @param waiting
     * @param placing
     * @param playing
     */
    public Client(BooleanProperty waiting, BooleanProperty placing, BooleanProperty playing) {
        this();
        Client.waiting = waiting;
        Client.placing = placing;
        Client.playing = playing;
    }

    /**
     * Invia una richiesta di connessione al server e attende una risposta.
     * Se la connessione va a buon fine, viene creato un thread per gestire la comunicazione.
     *
     * @param IPAddress
     * @param port
     * @param username
     * @return
     * @throws IOException
     */
    public boolean connect(String IPAddress, int port, String username) throws IOException {

        Client.sock = new DatagramSocket();
        Client.IPAddress = InetAddress.getByName(IPAddress);
        Client.port = port;
        Client.username = username;

        // invio richiesta di connessione
        sendMessage(sock, "username " + Client.username, Client.IPAddress, Client.port);

        // ricezione risposta
        Message response = receiveMessage(sock);

        if (response.getText().toLowerCase().trim().equals("ok")) {

            // connessione accettata
            System.out.println("connesso");

            Client c = new Client();
            Thread t = new Thread(c);
            t.start();
            return true;

        }else{

            // connessione rifiutata
            System.out.println("connessione rifiutata");
            return false;

        }

    }

    /**
     * Disconnette l'utente dal server.
     */
    public void disconnect() {
        try {
            sendMessage(sock, "disconnect " + username, IPAddress, port);
        } catch (IOException e) {
            // errore durante l'invio
            e.printStackTrace();
        }
        sock.close();
        reinit();
    }

    /**
     * Invia al server il nome della nave da posizionare, la sua lunghezza,
     * le sue coordinate e orientamento, e attende un riscontro.
     *
     * @param name
     * @param startRow
     * @param startCol
     * @param length
     * @param orientation
     * @return
     */
    public boolean placeShip(String name, int startRow, char startCol, int length, String orientation) {

        String message = "placement " + username + ' ' + name + ' ' + startRow + ' ' + startCol + ' ' + length + ' ' + orientation;

        try {
            sendMessage(sock, message, IPAddress, port);
            Message receivedMessage = receiveMessage(sock);
            if (receivedMessage.getText().trim().equals("ok")) return true;
        } catch (IOException e) {
            System.out.println("Errore nell'invio del messaggio");
            return false;
        }

        return false;

    }

    /**
     * Invia al server il movimento da effettuare e attende un riscontro; poi il riscontro viene restituito come stringa.
     * Questo è utile per comprendere l'azione da eseguire nell'interfaccia grafica.
     *
     * @param col
     * @param row
     * @return
     */
    public String sendMove(char col, int row) {

        String message = "play " + username + ' ' + col + ' ' + row;

        try {
            sendMessage(sock, message, IPAddress, port);
            Message receivedMessage = receiveMessage(sock);

            if (receivedMessage.getText().trim().equals("miss")) {
                System.out.println("mancato");
                return "miss";
            } else if (receivedMessage.getText().trim().equals("hit")) {
                hits++;
                System.out.println("colpito");
                return "hit";
            } else if (receivedMessage.getText().trim().equals("invalid")) {
                return "invalid";
            }

        } catch (IOException e) {
            System.out.println("Errore nell'invio del messaggio");
            return "error";
        }

        return "error";

    }

    /**
     * Il thread, a seconda della fase di gioco, esegue un'azione diversa.
     */
    @Override
    public void run() {

        while (waiting.getValue()) {
            try {
                Message message = receiveMessage(sock);
                if(message.getText().toLowerCase().trim().equals("place")) {
                    waiting.setValue(false);
                    placing.setValue(true);
                    System.out.println("Inizia a posizionare...");
                }
            } catch (IOException e) {
                // errore nella ricezione del messaggio
                e.printStackTrace();
            }
        }

        while (placing.getValue()) { }

        try {
            Message message = receiveMessage(sock);
            String [] splittedMessage = message.getText().toLowerCase().trim().split(" ");
            if(splittedMessage[0].equals("completed")) {
                totHits = Integer.parseInt(splittedMessage[1]);
                playing.setValue(true);
                System.out.println("Inizia a giocare...");
            }
        } catch (IOException e) {
            // errore nella ricezione del messaggio
            e.printStackTrace();
        }

        boolean jump = false;

        while (playing.getValue())

            if (!finished) {
                if (checkHits()) playing.setValue(false);
            } else {
                System.out.println("Game Over!");
                jump = true;
                break;
            }

        if (!finished && !jump) {
            finished = true;
        }

    }

    /**
     * Questo metodo viene chiamato dal Task avviato dall'interfaccia grafica per ricevere un nuovo messaggio.
     * @return
     */
    public String receiveMove() {
        try {
            Message message = receiveMessage(sock);
            return message.getText();
        } catch (IOException e) {
            // errore nella ricezione del messaggio
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Controlla se il numero di colpi è uguale a quello totale; se sì, restituisce true, altrimenti false.
     * @return
     */
    private boolean checkHits() {
        return (hits == totHits);
    }

    private void reinit() {
        username = null;
        hits = 0;
        totHits = 0;
        finished = false;
    }

}