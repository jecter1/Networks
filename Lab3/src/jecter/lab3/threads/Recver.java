package jecter.lab3.threads;

import jecter.lab3.Neighbour;
import jecter.lab3.Node;
import jecter.lab3.exceptions.NeighbourNotFoundException;
import jecter.lab3.protocol.Message;
import jecter.lab3.protocol.MessageHeader;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Recver implements Runnable {
    Node node;

    public Recver(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message message;
            try {
                byte[] buf = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                node.getSocket().receive(packet);

                // Packet loss simulation
                if (ThreadLocalRandom.current().nextInt(MIN_LOSS, MAX_LOSS) < node.getLoss()) continue;

                InetAddress address = packet.getAddress(); // sender's IP
                int port = packet.getPort(); // sender's port

                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
                     ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

                    message = (Message) objectInputStream.readObject();

                    UUID id = message.getID(); // ID of the message

                    String srcName = message.getName(); // Name of the source node
                    String rName = node.getName(); // Own node's name

                    MessageHeader header = message.getHeader();

                    // String debugPrintf;
                    switch (header) {
                        case REQUEST -> {
                            Neighbour sender = new Neighbour(address, port, srcName);;

                            if (!node.isRecvMessage(id)) {
                                node.getNeighbours().add(sender);
                                System.out.println("New neighbour (" + srcName + ")");
                            }
                            node.addRecvMessage(id);

                            Message cMessage = new Message(id, MessageHeader.CONFIRMATION, rName, REQUEST_CONFIRM_MSG);
                            Set<Neighbour> cReceiver = new HashSet<>();
                            cReceiver.add(sender);
                            node.addMessage(cMessage, cReceiver);

                            // String sName = sender.getName();
                            // debugPrintf = "(" + id + ") [<-" + sName + "] request";
                        }
                        case CONFIRMATION -> {
                            Neighbour sender = node.findNeighbour(address, port);

                            if (sender.getName().equals(Neighbour.DEFAULT_NAME)) {
                                sender.setName(srcName);
                                System.out.println("New neighbour (" + srcName + ")");
                            }

                            if (node.getReceiversByID(id).size() == SIZE_ONE &&
                                node.getReceiversByID(id).contains(sender) &&
                                node.getMessageByID(id).getName().equals(rName) &&
                                !message.getText().equals(REQUEST_CONFIRM_MSG)) {
                                // there is only one receiver left (this one)
                                // and message was sent by this node
                                // print delivered message
                                String messageText = node.getMessageByID(id).getText();
                                System.out.println("Message \"" + messageText + "\" was delivered");
                            }
                            node.confirmReceive(id, sender);

                            // String sName = sender.getName();
                            // debugPrintf = "(" + id + ") [<-" + sName + "] confirm";
                        }
                        case MESSAGE -> {
                            Neighbour sender = node.findNeighbour(address, port);

                            if (!node.isRecvMessage(id)) {
                                UUID newID = UUID.randomUUID();
                                Message resendMessage = new Message(newID, header, srcName, message.getText());
                                Set<Neighbour> receivers = new HashSet<>(node.getNeighbours());
                                receivers.remove(sender);
                                if (!receivers.isEmpty()) node.addMessage(resendMessage, receivers);

                                System.out.println(srcName + ": " + message.getText());
                            }
                            node.addRecvMessage(id);

                            Message cMessage = new Message(id, MessageHeader.CONFIRMATION, rName, EMPTY_STRING);

                            Set<Neighbour> cReceiver = new HashSet<>();
                            cReceiver.add(sender);
                            node.addMessage(cMessage, cReceiver);

                            // String sName = sender.getName(); // sender's name
                            // debugPrintf = "(" + id + ") [<-" + sName + "] " + srcName + ": " + message.getText();
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + header);
                    }
                    // System.out.println(debugPrintf);

                }
            } catch (IOException | ClassNotFoundException | NeighbourNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int SIZE_ONE = 1;

    private static final int MIN_LOSS = 0;
    private static final int MAX_LOSS = 100;

    private static final int BUFFER_SIZE = 8192;

    private static final String REQUEST_CONFIRM_MSG = "request";

    private static final String EMPTY_STRING = "";
}
