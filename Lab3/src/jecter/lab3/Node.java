package jecter.lab3;

import jecter.lab3.exceptions.IncorrectArgumentsException;
import jecter.lab3.exceptions.NeighbourNotFoundException;
import jecter.lab3.protocol.Message;
import jecter.lab3.protocol.MessageHeader;
import jecter.lab3.threads.Reader;
import jecter.lab3.threads.Recver;
import jecter.lab3.threads.Sender;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Node {
    private final String name;
    private final int loss;
    private final DatagramSocket socket;
    private final Set<Neighbour> neighbours = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Message> messages = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Neighbour>> messageReceivers = new ConcurrentHashMap<>();
    private final Queue<UUID> recvMessages = new ConcurrentLinkedQueue<>();
    private boolean connected = false;

    public Node(String name, int port, int loss) throws IncorrectArgumentsException, SocketException {
        this.name = name;
        this.loss = loss;
        this.socket = new DatagramSocket(port);

        if (loss < MIN_LOSS || loss > MAX_LOSS) throw new IncorrectArgumentsException();
    }

    public void connect(InetAddress inetAddress, int port) {
        if (connected) return;

        Neighbour neighbour = new Neighbour(inetAddress, port);
        this.neighbours.add(neighbour);

        Set<Neighbour> neighbours = new HashSet<>();
        neighbours.add(neighbour);

        UUID id = UUID.randomUUID();
        Message message = new Message(id, MessageHeader.REQUEST, name, EMPTY_STRING);

        messages.put(id, message);
        messageReceivers.put(id, neighbours);

        connected = true;
    }

    public void startCommunication() {
        Thread threadReader = new Thread(new Reader(this));
        Thread threadRecver = new Thread(new Recver(this));
        Thread threadSender = new Thread(new Sender(this));

        threadReader.start();
        threadRecver.start();
        threadSender.start();
    }


    public String getName() {
        return name;
    }

    public int getLoss() {
        return loss;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public Set<Neighbour> getNeighbours() {
        return neighbours;
    }


    public Neighbour findNeighbour(InetAddress address, int port) throws NeighbourNotFoundException {
        for (var neighbour: neighbours) {
            if (neighbour.getAddress().equals(address) && neighbour.getPort() == port) {
                return neighbour;
            }
        }
        throw new NeighbourNotFoundException();
    }

    public Set<UUID> getSetID() { return messages.keySet(); }

    public void addMessage(Message message, Set<Neighbour> receivers) {
        UUID id = message.getID();
        if (!messages.containsKey(id)) {
            messages.put(id, message);
            messageReceivers.put(id, receivers);
        } else {
            for (var receiver : receivers) {
                messageReceivers.get(id).add(receiver);
            }
        }
    }

    public Message getMessageByID(UUID id) {
        return messages.get(id);
    }

    public Set<Neighbour> getReceiversByID(UUID id) {
        return messageReceivers.get(id);
    }

    public void confirmReceive(UUID id, Neighbour neighbour) {
        if (messageReceivers.get(id) == null) return;

        messageReceivers.get(id).remove(neighbour);
        if (messageReceivers.get(id).isEmpty()) {
            messages.remove(id);
            messageReceivers.remove(id);
        }
    }


    public void addRecvMessage(UUID id) {
        if (recvMessages.contains(id)) return;
        if (recvMessages.size() == MAX_RECV_MESSAGES_SIZE) {
            recvMessages.remove();
        }
        recvMessages.add(id);
    }

    public boolean isRecvMessage(UUID id) {
        return recvMessages.contains(id);
    }

    private static final int MIN_LOSS = 0;
    private static final int MAX_LOSS = 100;

    private static final int MAX_RECV_MESSAGES_SIZE = 1000;

    private static final String EMPTY_STRING = "";
}
