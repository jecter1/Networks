package jecter.lab3.threads;

import jecter.lab3.Neighbour;
import jecter.lab3.Node;
import jecter.lab3.protocol.Message;
import jecter.lab3.protocol.MessageHeader;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class Reader implements Runnable {
    Node node;

    public Reader(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (!Thread.currentThread().isInterrupted()) {
                UUID id = UUID.randomUUID();
                String text = scanner.nextLine();

                Message message = new Message(id, MessageHeader.MESSAGE, node.getName(), text);
                Set<Neighbour> receivers = new HashSet<>(node.getNeighbours());
                node.addMessage(message, receivers);
            }
        }
    }
}
