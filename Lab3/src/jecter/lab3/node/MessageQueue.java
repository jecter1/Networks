package jecter.lab3.node;

import jecter.lab3.communication.Message;

import java.util.*;

public class MessageQueue {
    private final Map<Message, Set<Neighbour>> messagesWithReceivers;
    private final String nodeName;


    public MessageQueue(String nodeName) {
        messagesWithReceivers = new HashMap<>();
        this.nodeName = nodeName;
    }

    public synchronized void add(Message message, Set<Neighbour> receivers) {
        if (receivers.isEmpty()) {
            printNoReceiversIfMessageIsTextAndNodeIsSender(message);
            return;
        }
        messagesWithReceivers.put(message, receivers);
    }

    private void printNoReceiversIfMessageIsTextAndNodeIsSender(Message message) {
        if (isMessageText(message) && isNodeSender(message)) {
            System.out.println("[THERE ARE NO RECEIVERS FOR MESSAGE \"" + message.getText() + "\"]");
        }
    }

    private boolean isMessageText(Message message) {
        return message.is(Message.Header.TEXT);
    }

    private boolean isNodeSender(Message message) {
        return message.getSourceName().equals(nodeName);
    }

    public void add(Message message, Neighbour receiver) {
        Set<Neighbour> receivers = new HashSet<>();
        receivers.add(receiver);
        add(message, receivers);
    }

    public synchronized boolean contains(Message message) {
        return messagesWithReceivers.containsKey(message);
    }

    public synchronized Set<Message> getAllMessages() {
        return new HashSet<>(messagesWithReceivers.keySet());
    }

    public synchronized Set<Neighbour> getReceivers(Message message) throws NoSuchElementException {
        Set<Neighbour> receivers = messagesWithReceivers.get(message);
        if (receivers != null) {
            return new HashSet<>(receivers);
        } else {
            throw new NoSuchElementException();
        }
    }

    public synchronized void remove(Message message, Neighbour neighbour) {
        Set<Neighbour> receivers = messagesWithReceivers.get(message);
        receivers.remove(neighbour);
        removeMessageIfHasNoReceivers(message);
    }

    private synchronized void removeMessageIfHasNoReceivers(Message message) {
        Message realMessage = findMessage(message);
        if (messagesWithReceivers.get(realMessage).isEmpty()) {
            messagesWithReceivers.remove(realMessage);
            printDeliveredIfMessageIsTextAndNodeIsSender(realMessage);
        }
    }

    private synchronized Message findMessage(Message equalMessage) {
        Set<Message> messages = getAllMessages();
        for (var message : messages) {
            if (message.equals(equalMessage)) {
                return message;
            }
        }
        throw new RuntimeException();
    }

    private void printDeliveredIfMessageIsTextAndNodeIsSender(Message message) {
        if (isMessageText(message) && isNodeSender(message)) {
            System.out.println("[MESSAGE \"" + message.getText() + "\" WAS DELIVERED]");
        }
    }

    public synchronized void removeNeighbourFromReceivers(Neighbour neighbour) {
        Set<Message> messages = messagesWithReceivers.keySet();
        for (var message : messages) {
            Set<Neighbour> neighbours = messagesWithReceivers.get(message);
            neighbours.removeIf(neighbour::equals);
        }
        messages.removeIf(m -> messagesWithReceivers.get(m).isEmpty());
    }
}
