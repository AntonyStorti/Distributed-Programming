package unisa.distributedprogramming.message;
import java.net.InetAddress;


public class Message {

    private String text;
    private int port = -1;
    private InetAddress address = null;

    public Message(String text) {
        this.text = text;
    }

    public Message(String text, int port, InetAddress address) {
        this(text);
        this.port = port;
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

}
