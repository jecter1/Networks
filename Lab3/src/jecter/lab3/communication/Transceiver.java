package jecter.lab3.communication;

import jecter.lab3.communication.exceptions.CommunicationException;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class Transceiver implements AutoCloseable, Addressable {
    private static final String OPEN_SOCKET_EXCEPTION_MESSAGE = "Can't open socket";


    private final DatagramSocket socket;
    private final Receiver receiver;
    private final Sender sender;
    private final InetSocketAddress address;


    public Transceiver(InetSocketAddress address, int lossPercent) {
        this.socket = createSocket(address);
        this.receiver = new Receiver(socket, lossPercent);
        this.sender = new Sender(socket);
        this.address = address;
    }

    private DatagramSocket createSocket(InetSocketAddress address) {
        try {
            return new DatagramSocket(address);
        } catch (SocketException e) {
            throw new RuntimeException(OPEN_SOCKET_EXCEPTION_MESSAGE);
        }
    }

    public Message receive() throws CommunicationException {
        return receiver.receive();
    }

    public Addressable getLastReceiveSource() {
        return receiver::getLastAddress;
    }

    public void send(Message message, Addressable destination) throws CommunicationException {
        SocketAddress address = destination.getAddress();
        sender.send(message, address);
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }
}
