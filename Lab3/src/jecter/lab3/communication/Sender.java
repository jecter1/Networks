package jecter.lab3.communication;

import jecter.lab3.communication.exceptions.CommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class Sender {
    private static final int BUFFER_SIZE = 8192;


    private final DatagramSocket socket;
    private byte[] buffer;


    public Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    public void send(Message message, SocketAddress address) throws CommunicationException {
        writeToBuffer(message);
        sendFromBuffer(address);
    }

    private void writeToBuffer(Message message) throws CommunicationException {
        try {
            tryToWriteToBuffer(message);
        } catch (Exception e) {
            throw new CommunicationException();
        }
    }

    private void tryToWriteToBuffer(Message message) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
            buffer = byteArrayOutputStream.toByteArray();
        }
    }

    private void sendFromBuffer(SocketAddress address) throws CommunicationException {
        try {
            tryToSendFromBuffer(address);
        } catch (Exception e) {
            throw new CommunicationException();
        }
    }

    private void tryToSendFromBuffer(SocketAddress address) throws Exception {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);
        socket.send(packet);
    }
}
