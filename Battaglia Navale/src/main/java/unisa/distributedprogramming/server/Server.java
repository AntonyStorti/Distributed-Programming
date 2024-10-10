package unisa.distributedprogramming.server;

import unisa.distributedprogramming.Communicator;
import unisa.distributedprogramming.message.Message;
import unisa.distributedprogramming.table.Player;
import unisa.distributedprogramming.table.Table;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;


public class Server extends Communicator implements Runnable {

    private static DatagramSocket sock;

    private static Map<String, Player> users = new HashMap<>(); // contiene la mappatura tra il nome utente e il giocatore
    // con quell'username
    private static Map<Player, Table> tables = new HashMap<>(); // contiene la mappatura tra il giocatore e il tavolo a cui appartiene

    private static String toHandle = null; // fase dell'interazione con il client da gestire da parte del server

    private Player waitingUser = null; // utente nella sala d'attesa

    private Player player1 = null; // giocatore1 nel tavolo che il server sta attualmente gestendo
    private Player player2 = null; // giocatore2 nel tavolo che il server sta attualmente gestendo
    private Table table = null; // tavolo che il server sta attualmente gestendo
    private String toDo = null; // azione da eseguire durante il movimento
    private int row = -1;
    private char col = ' '; // riga e colonna da utilizzare dal thread quando gestisce il movimento da effettuare

    /**
     * Crea il server.
     *
     * @param port
     * @throws IOException
     */
    public Server(int port) throws IOException {
        sock = new DatagramSocket(port);
    }

    /**
     * Crea un'istanza del server che gestisce il tavolo con l'utente 1 e l'utente 2.
     *
     * @param player1
     * @param player2
     * @param table
     */
    public Server(Player player1, Player player2, Table table) {
        this.player1 = player1;
        this.player2 = player2;
        this.table = table;
    }

    /**
     * Crea un'istanza del server che gestisce il tavolo con l'utente 1 e l'utente 2 e ha un'azione specifica da eseguire.
     *
     * @param player1
     * @param player2
     * @param table
     * @param toDo
     */
    public Server(Player player1, Player player2, Table table, String toDo, int row, char col) {
        this.player1 = player1;
        this.player2 = player2;
        this.table = table;
        this.toDo = toDo;
        this.row = row;
        this.col = col;
    }

    /**
     * Metodo chiamato dal server in un ciclo infinito, che riceve solo messaggi.
     *
     * @throws IOException
     */
    public void acceptMessage() throws IOException {

        Message message = receiveMessage(sock);
        String[] splittedMessage = message.getText().trim().split(" ");

        if (splittedMessage[0].equals("username")) {

            toHandle = "username";
            handleConnection(splittedMessage, message);

        } else if (splittedMessage[0].equals("placement")) {

            toHandle = "placement";
            handlePlacement(splittedMessage);

        } else if (splittedMessage[0].equals("play")) {

            toHandle = "play";
            handleMove(splittedMessage);

        } else if (splittedMessage[0].equals("disconnect")) {

            handleDisconnection(splittedMessage);

        }

    }

    /**
     * Se il nome utente è già in uso, la connessione viene rifiutata e viene inviato un messaggio al client per avvisarlo.
     * Altrimenti, se il nome utente è valido, se c'è già un utente nella sala d'attesa, viene creato un tavolo da gioco;
     * altrimenti, l'utente viene messo nella sala d'attesa."
     *
     * @param splittedMessage
     * @param usernameMessage
     * @throws IOException
     */
    private void handleConnection(String[] splittedMessage, Message usernameMessage) throws IOException {

        String username = splittedMessage[1];

        InetAddress IPAddress = usernameMessage.getAddress();
        int port = usernameMessage.getPort();

        if (!users.containsKey(username)) {

            // username valido
            Player player = new Player(username, IPAddress, port);
            users.put(username, player);

            sendMessage(sock, "ok", IPAddress, port);
            System.out.println(" - Utente " + username + " connesso correttamente.");

            if (waitingUser == null) {

                // nessun utente nella sala d'attesa
                waitingUser = player;
                System.out.println(" - Utente " + username + " nella sala d'attesa.");

            } else {

                // c'è già un utente nella sala d'attesa
                Table newTable = new Table(waitingUser, player);
                tables.put(waitingUser, newTable);
                tables.put(player, newTable);
                System.out.println(" - Tavolo di gioco creato per " + username + " e " + waitingUser.getUsername());

                // crea un nuovo thread per gestire il tavolo
                Server s = new Server(player, waitingUser, newTable);
                waitingUser = null;
                Thread t = new Thread(s);
                t.start();
            }

        } else {
            // username non valido
            sendMessage(sock, "connection refused", IPAddress, port);
            System.out.println(" - ERRORE: " + username + " non è valido. Connessione rifiutata.");
        }
    }

    /**
     * Posiziona la nave nel tabellone di gioco e avvia un thread per comunicare con il client.
     *
     * @param splittedMessage
     */
    private void handlePlacement(String[] splittedMessage) {

        Player sender = users.get(splittedMessage[1]);

        Table senderTable = tables.get(sender);
        Player other = senderTable.getOther(sender);

        // posiziona la nave nella matrice di posizionamento
        senderTable.getGameBoard(sender).placeShip(splittedMessage[2], Integer.parseInt(splittedMessage[3]),
                splittedMessage[4].charAt(0), Integer.parseInt(splittedMessage[5]), splittedMessage[6]);

        // crea un nuovo thread per gestire il posizionamento
        Server s = new Server(sender, other, senderTable);
        Thread t = new Thread(s);
        t.start();

    }

    /**
     * Elabora la mossa effettuata dall'utente e crea un thread per fornirgli un riscontro al riguardo.
     *
     * @param splittedMessage
     */
    private void handleMove(String[] splittedMessage) {

        Player sender = users.get(splittedMessage[1]);
        Table senderTable = tables.get(sender);
        Player other = senderTable.getOther(sender);

        char col = splittedMessage[2].charAt(0);
        int row = Integer.parseInt(splittedMessage[3]);
        String toDo = senderTable.getGameBoard(other).makeMove(col, row);

        // crea un nuovo thread per gestire la mossa
        Server s = new Server(sender, other, senderTable, toDo, row, col);
        Thread t = new Thread(s);
        t.start();

    }

    /**
     * Questo metodo gestisce la disconnessione dell'utente.
     * @param splittedMessage
     */
    private void handleDisconnection(String[] splittedMessage) {

        Player sender = users.get(splittedMessage[1]);
        users.remove(sender.getUsername());
        tables.remove(sender);

        System.out.println("Utenti ancora attivi:");
        for (String p : users.keySet())
            System.out.println(p);

    }

    /**
     * Il thread invia solo messaggi all'utente, che sono diversi a seconda del contesto.
     */
    @Override
    public void run() {

        if (toHandle.equals("username")) {

            try {
                sendMessage(sock, "place", player1.getIPaddress(), player1.getPort());
                sendMessage(sock, "place", player2.getIPaddress(), player2.getPort());
            } catch (IOException e) {
                // errore nell'invio del messaggio
                e.printStackTrace();
            }

        } else if (toHandle.equals("placement")) {

            try {

                if (!table.getGameBoard(player1).isPlacementCompleted()
                        || !table.getGameBoard(player2).isPlacementCompleted())
                    // player 1 è il mittente
                    sendMessage(sock, "ok", player1.getIPaddress(), player1.getPort());
                else {
                    sendMessage(sock, "ok", player1.getIPaddress(), player1.getPort());
                    sendMessage(sock, "completed " + table.getGameBoard(player2).getTotHits(), player1.getIPaddress(),
                            player1.getPort());
                    sendMessage(sock, "completed " + table.getGameBoard(player1).getTotHits(), player2.getIPaddress(),
                            player2.getPort());
                    sendMessage(sock, "yourturn ", player1.getIPaddress(), player1.getPort());
                    sendMessage(sock, "wait ", player2.getIPaddress(), player2.getPort());
                    System.out.println("completato");
                }

            } catch (IOException e) {
                // errore nell'invio del messaggio
                e.printStackTrace();
            }

        } else if (toHandle.equals("play")) {

            try {

                if (toDo.equals("hit")) {
                    // messaggio di hit al mittente
                    sendMessage(sock, "hit", player1.getIPaddress(), player1.getPort());

                    if (table.getGameBoard(player2).isLoser())
                        sendMessage(sock, "loser", player2.getIPaddress(), player2.getPort());
                    else
                        sendMessage(sock, "move " + col + ' ' + row, player2.getIPaddress(), player2.getPort());

                } else if (toDo.equals("miss")) {
                    // messaggio di miss al mittente
                    sendMessage(sock, "miss", player1.getIPaddress(), player1.getPort());
                    sendMessage(sock, "yourturn", player2.getIPaddress(), player2.getPort());

                } else {
                    // messaggio non valido al mittente
                    sendMessage(sock, "invalid", player1.getIPaddress(), player1.getPort());
                }
            } catch (IOException e) {
                // errore nell'invio del messaggio
                e.printStackTrace();
            }

        }

    }

    public static void main(String args[]) {
        try {
            Server s = new Server(12345);
            System.out.println("Avvio del server...");
            while (true)
                s.acceptMessage();
        } catch (IOException e) {
            System.out.println("Impossibile creare il socket del server");
        }
    }

}