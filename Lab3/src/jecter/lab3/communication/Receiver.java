package jecter.lab3.communication;

import jecter.lab3.communication.exceptions.CommunicationException;
import jecter.lab3.communication.exceptions.LostMessageException;
import jecter.lab3.communication.exceptions.NotMessageException;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

public class Receiver {
    private static final int BUFFER_SIZE = 8192;


    private final DatagramSocket socket;
    private final int lossPercent;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    private InetSocketAddress lastAddress;


    public Receiver(DatagramSocket socket, int lossPercent) {
        this.socket = socket;
        this.lossPercent = lossPercent;
    }

    public Message receive() throws CommunicationException {
        receiveToBuffer();
        throwExceptionIfLost();
        return readFromBuffer();
    }

    private void receiveToBuffer() throws CommunicationException {
        try {
            tryToReceiveToBuffer();
        } catch (Exception e) {
            throw new CommunicationException();
        }
    }

    private void tryToReceiveToBuffer() throws Exception {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        lastAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
    }

    private void throwExceptionIfLost() throws LostMessageException {
        if (isLost()) {
            throw new LostMessageException();
        }
    }

    private boolean isLost() {
        final int min = 0;
        final int max = 100;
        return (randomInteger(min, max) < lossPercent);
    }

    private int randomInteger(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    private Message readFromBuffer() throws CommunicationException {
        try {
            return tryToReadFromBuffer();
        } catch (ClassNotFoundException e) {
            throw new NotMessageException();
        } catch (Exception e) {
            throw new CommunicationException();
        }
    }

    private Message tryToReadFromBuffer() throws Exception {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (Message) objectInputStream.readObject();
        }
    }

    public InetSocketAddress getLastAddress() {
        return lastAddress;
    }
}
