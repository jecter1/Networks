package jecter.lab3.node;

import jecter.lab3.communication.Addressable;
import jecter.lab3.communication.exceptions.CommunicationException;
import jecter.lab3.communication.exceptions.LostMessageException;
import jecter.lab3.communication.Message;
import jecter.lab3.communication.Transceiver;
import jecter.lab3.communication.exceptions.NotMessageException;

import java.util.*;

public class Node {
    private final String name;
    private final Transceiver transceiver;
    private final Environment environment;
    private final MessageQueue messageQueue;
    private final MessageStatistics messageStatistics;

    private boolean communicating = true;


    public Node(String name, Transceiver transceiver) {
        this.name = name;
        this.transceiver = transceiver;
        this.environment = new Environment();
        this.messageQueue = new MessageQueue(name);
        this.messageStatistics = new MessageStatistics();
    }

    public Node(String name, Transceiver transceiver, Neighbour parent) {
        this(name, transceiver);
        addRequestMessageToQueue(parent);
    }

    private void addRequestMessageToQueue(Neighbour neighbour) {
        Message requestMessage = new Message(Message.Header.REQUEST, name);
        messageQueue.add(requestMessage, neighbour);
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
                while (communicating) {
                    sendAllMessagesToAllReceivers();
                    removeNotRespondingNeighbours();
                    ping();
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
                if (message.is(Message.Header.CONFIRMATION)) {
                    messageQueue.remove(message, receiver);
                }
            }

            private void removeNotRespondingNeighbours() {
                Set<Neighbour> neighbours = environment.getNeighbours();
                for (var neighbour : neighbours) {
                    removeNeighbourIfNorResponding(neighbour);
                }
            }

            private void removeNeighbourIfNorResponding(Neighbour neighbour) {
                if (!neighbour.isResponding()) {
                    removeNeighbour(neighbour);
                }
            }

            private void removeNeighbour(Neighbour neighbour) {
                environment.removeNeighbour(neighbour);
                messageQueue.removeNeighbourFromReceivers(neighbour);
                try {
                    Substitute neighbourSubstitute = neighbour.getSubstitute();
                    if (!(neighbourSubstitute.getAddress().getAddress().isAnyLocalAddress() ||
                            neighbourSubstitute.getAddress().getAddress().isLoopbackAddress()) ||
                            neighbourSubstitute.getAddress().getPort() != transceiver.getAddress().getPort()) {
                        Neighbour neighbourToConnect = new Neighbour(neighbourSubstitute);
                        addRequestMessageToQueue(neighbourToConnect);
                    }
                } catch (Exception ignore) { }
            }

            private void ping() {
                Message pingMessage = new Message(Message.Header.PING, name);
                pingMessage.addSubstitute(environment.getSubstitute());
                Set<Neighbour> receivers = environment.getNeighbours();
                sendMessageToEachReceiverInSet(pingMessage, receivers);
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
                while (communicating) {
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
                currentSender = makeSender();
                try {
                    currentSender = environment.findNeighbour(currentSender);
                } catch (Exception e) {
                    environment.addNeighbour(currentSender);
                }
            }

            private Neighbour makeSender() {
                Addressable source = transceiver.getLastReceiveSource();
                String sourceName = currentMessage.getSourceName();
                return new Neighbour(source, sourceName);
            }

            private void handleMessage() {
                switch (currentMessage.getHeader()) {
                    case REQUEST -> handleRequest();
                    case TEXT -> handleText();
                    case CONFIRMATION -> handleConfirmation();
                    case PING -> handlePing();
                }
            }

            private void handleRequest() {
                if (!messageStatistics.isReceived(currentMessage)) {
                    messageStatistics.addReceivedMessage(currentMessage);
                }
                addConfirmationToMessages();
            }

            private void addConfirmationToMessages() {
                Message confirmMessage = new Message(currentMessage.getID(), Message.Header.CONFIRMATION, name);
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
                String name = currentMessage.getSourceName();
                String text = currentMessage.getText();
                System.out.println(name + ": " + text);
            }

            private void resendTextToOtherNeighbours() {
                Message resendMessage = makeResendMessage();
                Set<Neighbour> receivers = makeResendReceivers();
                messageQueue.add(resendMessage, receivers);
            }

            private Message makeResendMessage() {
                Message.Header header = currentMessage.getHeader();
                String sourceName = currentMessage.getSourceName();
                String text = currentMessage.getText();
                Message resendMessage = new Message(header, sourceName);
                resendMessage.addText(text);
                return resendMessage;
            }

            private Set<Neighbour> makeResendReceivers() {
                Set<Neighbour> receivers = environment.getNeighbours();
                receivers.remove(currentSender);
                return receivers;
            }

            private void handleConfirmation() {
                if (!messageStatistics.isReceived(currentMessage)) {
                    messageQueue.remove(currentMessage, currentSender);
                    setReceivedIfNoLongerContainsInQueue();
                }
            }

            private void setReceivedIfNoLongerContainsInQueue() {
                if (!messageQueue.contains(currentMessage)) {
                    messageStatistics.addReceivedMessage(currentMessage);
                }
            }

            private void handlePing() {
                currentSender.updateTime();
                try {
                    Substitute substitute = currentMessage.getSubstitute();
                    currentSender.setSubstitute(substitute);
                } catch (Exception e) {
                    currentSender.getSubstitute().remove();
                }
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
                while (communicating) {
                    readAndPutMessage();
                }
            }

            private void readAndPutMessage() {
                String text = scanner.nextLine();
                addTextToQueue(text);
            }

            private void addTextToQueue(String text) {
                Message message = new Message(Message.Header.TEXT, name);
                message.addText(text);
                Set<Neighbour> receivers = environment.getNeighbours();
                messageQueue.add(message, receivers);
            }
        };
    }

    public void stopCommunication() {
        communicating = false;
    }
}
