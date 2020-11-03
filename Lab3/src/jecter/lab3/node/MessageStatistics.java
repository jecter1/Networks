package jecter.lab3.node;

import jecter.lab3.communication.Message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageStatistics {
    private static final int MAX_SIZE = 1000;


    private final Queue<Message> receivedMessages;


    public MessageStatistics() {
        receivedMessages = new ConcurrentLinkedQueue<>();
    }

    public void addReceivedMessage(Message message) {
        removeFirstMessageIfFull();
        receivedMessages.add(message);
    }

    private void removeFirstMessageIfFull() {
        if (isFull()) {
            receivedMessages.remove();
        }
    }

    private boolean isFull() {
        return (receivedMessages.size() == MAX_SIZE);
    }

    public boolean isReceived(Message message) {
        return receivedMessages.contains(message);
    }
}
