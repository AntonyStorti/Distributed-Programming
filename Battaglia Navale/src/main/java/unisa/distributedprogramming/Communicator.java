package unisa.distributedprogramming;

import unisa.distributedprogramming.message.Message;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public abstract class Communicator {

    public void sendMessage(DatagramSocket sock, String message, InetAddress IPAddress, int port) throws IOException {
        byte[] sendData;
        sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        sock.send(sendPacket);
    }

    public Message receiveMessage(DatagramSocket sock) throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        sock.receive(receivePacket);
        return new Message(new String(receivePacket.getData()), receivePacket.getPort(), receivePacket.getAddress());
    }

}
