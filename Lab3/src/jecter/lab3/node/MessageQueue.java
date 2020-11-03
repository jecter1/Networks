package jecter.lab3.node;

import jecter.lab3.communication.Message;

import java.util.*;

public class MessageQueue {
    private final Map<Message, Set<Neighbour>> messagesWithReceivers;


    public MessageQueue() {
        messagesWithReceivers = new HashMap<>();
    }

    public synchronized void add(Message message, Set<Neighbour> receivers) {
        messagesWithReceivers.put(message, receivers);
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
        if (messagesWithReceivers.get(message).isEmpty()) {
            messagesWithReceivers.remove(message);
        }
    }
}
