package jecter.lab3.node;

import jecter.lab3.communication.Addressable;
import jecter.lab3.communication.exceptions.CommunicationException;
import jecter.lab3.communication.exceptions.LostMessageException;
import jecter.lab3.communication.Message;
import jecter.lab3.communication.Transceiver;
import jecter.lab3.communication.exceptions.NotMessageException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private final String name;
    private final Transceiver transceiver;
    private final Set<Neighbour> neighbours;
    private final MessageQueue messageQueue;
    private final MessageStatistics messageStatistics;
    private boolean running = true;


    public Node(String name, Transceiver transceiver) {
        this.name = name;
        this.transceiver = transceiver;
        this.neighbours = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.messageQueue = new MessageQueue();
        this.messageStatistics = new MessageStatistics();
    }

    public Node(String name, Transceiver transceiver, Neighbour parent) {
        this(name, transceiver);
        addParent(parent);
    }

    private void addParent(Neighbour parent) {
        neighbours.add(parent);
        sendRequestToParent(parent);
    }

    private void sendRequestToParent(Neighbour parent) {
        Message requestMessage = new Message(Message.Header.REQUEST, name);
        messageQueue.add(requestMessage, parent);
    }

    public void startCommunication() {
        startThreadSender();
        startThreadReceiver();
        startReading();
    }

    private void startThreadSender() {
        Runnable sender = createSender();
        Thread threadSender = new Thread(sender);
        threadSender.start();
    }

    private Runnable createSender() {
        return new Runnable() {
            private static final int SLEEP_MS = 5;


            @Override
            public void run() {
                while (running) {
                    sendAllMessagesToAllReceivers();
                    sleep();
                }
            }

            private void sendAllMessagesToAllReceivers() {
                Set<Message> messageSet = messageQueue.getAllMessages();
                sendEachMessageInSetToAllReceivers(messageSet);
            }

            private void sendEachMessageInSetToAllReceivers(Set<Message> messageSet) {
                for (var message : messageSet) {
                    sendMessageToAllReceivers(message);
                }
            }

            private void sendMessageToAllReceivers(Message message) {
                try {
                    Set<Neighbour> receiverSet = messageQueue.getReceivers(message);
                    sendMessageToEachReceiverInSet(message, receiverSet);
                } catch (NoSuchElementException ignore) { }
            }

            private void sendMessageToEachReceiverInSet(Message message, Set<Neighbour> receiverSet) {
                for (var receiver : receiverSet) {
                    sendMessageToReceiver(message, receiver);
                }
            }

            private void sendMessageToReceiver(Message message, Neighbour receiver) {
                try {
                    tryToSendMessageToReceiver(message, receiver);
                } catch (CommunicationException e) {
                    stopCommunication();
                }
            }

            private void tryToSendMessageToReceiver(Message message, Neighbour receiver) throws CommunicationException {
                transceiver.send(message, receiver);
                removeIfConfirmation(message, receiver);
            }

            private void removeIfConfirmation(Message message, Neighbour receiver) {
                if (message.header.equals(Message.Header.CONFIRMATION)) {
                    messageQueue.remove(message, receiver);
                }
            }

            private void sleep() {
                try {
                    Thread.sleep(SLEEP_MS);
                } catch (InterruptedException e) {
                    stopCommunication();
                }
            }
        };
    }

    private void startThreadReceiver() {
        Runnable receiver = createReceiver();
        Thread threadReceiver = new Thread(receiver);
        threadReceiver.start();
    }

    private Runnable createReceiver() {
        return new Runnable() {
            private Message currentMessage;
            private Neighbour currentSender;


            @Override
            public void run() {
                while (running) {
                    receiveAndHandleMessage();
                }
            }

            private void receiveAndHandleMessage() {
                try {
                    tryToReceiveAndHandleMessage();
                } catch (NotMessageException | LostMessageException ignore) {
                } catch (CommunicationException e) {
                    stopCommunication();
                }
            }

            private void tryToReceiveAndHandleMessage() throws CommunicationException {
                currentMessage = transceiver.receive();
                findSenderOrAddToNeighbours();
                handleMessage();
            }

            private void findSenderOrAddToNeighbours() {
                initSender();
                try {
                    currentSender = findSender();
                } catch (Exception e) {
                    neighbours.add(currentSender);
                    printNewNeighbour();
                }
            }

            private void printNewNeighbour() {
                String name = currentSender.getName();
                System.out.println("[new neighbour: " + name + "]");
            }

            private void initSender() {
                Addressable source = transceiver.getLastReceiveSource();
                String sourceName = currentMessage.sourceName;
                currentSender = new Neighbour(source, sourceName);
            }

            private Neighbour findSender() {
                for (var neighbour : neighbours) {
                    if (neighbour.equals(currentSender)) {
                        setNameIfNameless(neighbour);
                        return neighbour;
                    }
                }
                throw new RuntimeException();
            }

            private void setNameIfNameless(Neighbour neighbour) {
                if (!neighbour.hasName()) {
                    String name = currentSender.getName();
                    neighbour.setName(name);
                    printNewNeighbour();
                }
            }

            private void handleMessage() {
                switch (currentMessage.header) {
                    case REQUEST -> handleRequest();
                    case TEXT -> handleText();
                    case CONFIRMATION -> handleConfirmation();
                }
            }

            private void handleRequest() {
                if (!messageStatistics.isReceived(currentMessage)) {
                    messageStatistics.addReceivedMessage(currentMessage);
                }
                addConfirmationToMessages();
            }

            private void addConfirmationToMessages() {
                Message confirmMessage = new Message(currentMessage.id, Message.Header.CONFIRMATION, name);
                messageQueue.add(confirmMessage, currentSender);
            }

            private void handleText() {
                if (!messageStatistics.isReceived(currentMessage)) {
                    printText();
                    resendTextToOtherNeighbours();
                    messageStatistics.addReceivedMessage(currentMessage);
                }
                addConfirmationToMessages();
            }

            private void printText() {
                String name = currentMessage.sourceName;
                String text = currentMessage.text;
                System.out.println(name + ": " + text);
            }

            private void resendTextToOtherNeighbours() {
                Message resendMessage = makeResendMessage();
                Set<Neighbour> receivers = makeResendReceivers();
                messageQueue.add(resendMessage, receivers);
            }

            private Message makeResendMessage() {
                Message.Header header = currentMessage.header;
                String sourceName = currentMessage.sourceName;
                String text = currentMessage.text;
                return new Message(header, sourceName, text);
            }

            private Set<Neighbour> makeResendReceivers() {
                Set<Neighbour> receivers = new HashSet<>(neighbours);
                receivers.remove(currentSender);
                return receivers;
            }

            private void handleConfirmation() {
                if (!messageStatistics.isReceived(currentMessage)) {
                    setMessageReceivedIfThisSenderIsLastReceiverOfThisMessage();
                    messageQueue.remove(currentMessage, currentSender);
                }
            }

            private void setMessageReceivedIfThisSenderIsLastReceiverOfThisMessage() {
                if (isSenderLastReceiverOfThisMessage()) {
                    messageStatistics.addReceivedMessage(currentMessage);
                    printDeliveredIfMessageIsTextAndNodeIsSource();
                }
            }

            private boolean isSenderLastReceiverOfThisMessage() {
                final int RECEIVERS_SIZE_ONE = 1;
                return (messageQueue.contains(currentMessage) &&
                        messageQueue.getReceivers(currentMessage).size() == RECEIVERS_SIZE_ONE &&
                        messageQueue.getReceivers(currentMessage).contains(currentSender));
            }

            private void printDeliveredIfMessageIsTextAndNodeIsSource() {
                Set<Message> messages = messageQueue.getAllMessages();
                for (var message : messages) {
                    if (isConfirmationForThisMessage(message) && isMessageText(message) && isNodeSource(message)) {
                        printDelivered(message);
                    }
                }
            }

            private boolean isConfirmationForThisMessage(Message message) {
                return message.equals(currentMessage);
            }

            private boolean isMessageText(Message message) {
                return message.header.equals(Message.Header.TEXT);
            }

            private boolean isNodeSource(Message message) {
                return message.sourceName.equals(name);
            }

            private void printDelivered(Message message) {
                System.out.println("[message \"" + message.text + "\" was delivered]");
            }
        };
    }

    private void startReading() {
        Runnable reader = createReader();
        reader.run();
    }

    private Runnable createReader() {
        return new Runnable() {
            private Scanner scanner;

            @Override
            public void run() {
                try (Scanner scanner = new Scanner(System.in)) {
                    this.scanner = scanner;
                    readAndPutMessagesWhileRunning();
                }
            }

            private void readAndPutMessagesWhileRunning() {
                while (running) {
                    readAndPutMessage();
                }
            }

            private void readAndPutMessage() {
                String text = scanner.nextLine();
                putMessage(text);
            }

            private void putMessage(String text) {
                Message message = new Message(Message.Header.TEXT, name, text);
                Set<Neighbour> receivers = new HashSet<>(neighbours);
                messageQueue.add(message, receivers);
            }
        };
    }

    public void stopCommunication() {
        running = false;
    }
}
