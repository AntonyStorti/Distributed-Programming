package unisa.distributedprogramming.table;
import java.net.InetAddress;


public class Player {

    @Override
    public String toString() {
        return "Player [username=" + username + ", IPaddress=" + IPaddress + ", port=" + port + "]";
    }

    private String username;
    private InetAddress IPaddress;
    private int port;

    public Player(String username, InetAddress IPaddress, int port) {
        this.username = username;
        this.IPaddress = IPaddress;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getIPaddress() {
        return IPaddress;
    }

    public int getPort() {
        return port;
    }

}
