package jecter.lab3.threads;

import jecter.lab3.Neighbour;
import jecter.lab3.Node;
import jecter.lab3.protocol.Message;
import jecter.lab3.protocol.MessageHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Sender implements Runnable {
    Node node;

    public Sender(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // Iterating by messages
            if (node.getSetID() == null) continue;
            Set<UUID> idSet = new HashSet<>(node.getSetID());
            for (var id : idSet) {
                Message message = node.getMessageByID(id);
                // Send message to all neighbours who haven't confirmed its receipt
                if (node.getReceiversByID(id) == null) continue;
                Set<Neighbour> neighbourSet = new HashSet<>(node.getReceiversByID(id));
                for (var neighbour : neighbourSet) {
                    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){

                        byte[] buffer;
                        objectOutputStream.writeObject(message);
                        buffer = byteArrayOutputStream.toByteArray();

                        InetAddress address = neighbour.getAddress();
                        int port = neighbour.getPort();

                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

                        node.getSocket().send(packet);

                        if (message.getHeader() == MessageHeader.CONFIRMATION) {
                            node.confirmReceive(id, neighbour);
                        }

                        /*
                        String debugPrintf;
                        switch (message.getHeader()) {
                            case CONFIRMATION -> {
                                debugPrintf = "(" + message.getID() + ") [->" + neighbour.getName() + "] confirm";
                            }
                            case REQUEST -> {
                                debugPrintf = "(" + message.getID() + ") [->" + neighbour.getName() + "] request";
                            }
                            case MESSAGE -> {
                                debugPrintf = "(" + message.getID() + ") [->" + neighbour.getName() + "] " +
                                              message.getName() + ": " + message.getText();
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + message.getHeader());
                        }
                        System.out.println(debugPrintf);
                        */

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final long SLEEP_TIME = 100;
}
