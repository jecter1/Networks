package jecter.lab1.threads;

import jecter.lab1.Constants;

import java.io.*;
import java.net.*;

public class Sender implements Runnable, Constants {
    private final InetAddress group;
    private boolean running;

    public Sender(InetAddress group) {
        this.group = group;
        this.running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            byte[] buffer = SPECIAL_MESSAGE.getBytes();
            while (running) {
                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, group, RECEIVER_PORT);
                sendSocket.send(sendPacket);

                Thread.sleep(SENDER_SLEEP_TIME_MS);
            }
        } catch (IOException | InterruptedException exc) {
            exc.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}