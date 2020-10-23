package jecter.lab1.threads;

import jecter.lab1.Constants;
import jecter.lab1.TimeTable;

import java.io.*;
import java.net.*;

public class Receiver implements Runnable, Constants {
    private final TimeTable timeTable;
    private final InetAddress group;
    private boolean running;

    public Receiver(TimeTable timeTable, InetAddress group) {
        this.timeTable = timeTable;
        this.group = group;
        this.running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try (MulticastSocket recvSocket = new MulticastSocket(RECEIVER_PORT)) {

            SocketAddress socketAddress = new InetSocketAddress(group, RECEIVER_PORT);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(group);

            recvSocket.joinGroup(socketAddress, networkInterface);
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
            while (running) {
                try {
                    recvSocket.receive(recvPacket);
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
                String message = new String(recvPacket.getData()).trim();
                if (!message.equals(SPECIAL_MESSAGE)) continue;
                String ipPort = recvPacket.getAddress().getHostAddress() + IP_PORT_SEPARATOR + recvPacket.getPort();
                timeTable.updateTime(ipPort);
            }
            recvSocket.leaveGroup(socketAddress, networkInterface);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }

    private static final String IP_PORT_SEPARATOR = ":";
}