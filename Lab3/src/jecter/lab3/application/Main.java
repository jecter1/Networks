package jecter.lab3.application;

import jecter.lab3.communication.Addressable;
import jecter.lab3.node.Neighbour;
import jecter.lab3.node.Node;
import jecter.lab3.communication.Transceiver;

import java.net.SocketAddress;

public class Main {
    private static Arguments arguments;


    public static void main(String[] args) {
        try {
            tryToStartProgram(args);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void tryToStartProgram(String[] args) {
        arguments = Arguments.parse(args);
        createNodeAndStartCommunication();
    }

    private static void createNodeAndStartCommunication() {
        try (Transceiver transceiver = createTransceiver()) {
            Node node = createNode(transceiver);
            node.startCommunication();
        }
    }

    private static Transceiver createTransceiver() {
        SocketAddress address = arguments.getAddress();
        int lossPercent = arguments.getLossPercent();
        return new Transceiver(address, lossPercent);
    }

    private static Node createNode(Transceiver transceiver) {
        String name = arguments.getName();
        return createNode(name, transceiver);
    }

    private static Node createNode(String name, Transceiver transceiver) {
        try {
            return createAttachedNode(name, transceiver);
        } catch (Exception e) {
            return createDetachedNode(name, transceiver);
        }
    }

    private static Node createAttachedNode(String name, Transceiver transceiver) {
        Neighbour parent = createParent();
        return new Node(name, transceiver, parent);
    }

    private static Neighbour createParent() {
        Addressable addressable = () -> arguments.getParentAddress();
        return new Neighbour(addressable);
    }

    private static Node createDetachedNode(String name, Transceiver transceiver) {
        return new Node(name, transceiver);
    }
}
