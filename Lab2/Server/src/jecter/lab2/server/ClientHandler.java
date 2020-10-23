package jecter.lab2.server;

import jecter.lab2.protocol.ProtocolConstants;
import jecter.lab2.protocol.ProtocolException;
import jecter.lab2.protocol.ProtocolMessage;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    private String filename;
    private long fileSize;
    private long recvSize = 0;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream  in  = new DataInputStream(clientSocket.getInputStream())) {
            System.out.println(clientSocket.getInetAddress().getHostAddress() + " connected");

            protocolSwap(in, out, ProtocolMessage.INFO);
            recvInfo(in);

            protocolSwap(in, out, ProtocolMessage.FILE);
            recvFile(in);

            sendResult(out);

        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ProtocolException exc) {
            System.out.println(exc.toString());
        }
    }

    private void protocolSwap(DataInputStream in, DataOutputStream out, ProtocolMessage message)
                                                                            throws ProtocolException, IOException {
        out.writeUTF(message.toString());
        String clientMessage = in.readUTF();
        if (!clientMessage.equals(message.toString())) {
            throw new ProtocolException();
        }
    }

    private void recvInfo(DataInputStream in) throws ProtocolException, IOException {
        filename = in.readUTF();
        fileSize = in.readLong();

        if (filename.getBytes().length > ProtocolConstants.FILEPATH_MAX_LENGTH_BYTES) {
            throw new ProtocolException();
        }
        if (fileSize > ProtocolConstants.FILE_MAX_SIZE_BYTES) {
            throw new ProtocolException();
        }
    }

    private void recvFile(DataInputStream in) {
        while (new File(Server.UPLOADS_DIRECTORY + File.separatorChar + filename).exists()) {
            filename += " (new)";
        }

        String filepath = Server.UPLOADS_DIRECTORY + File.separatorChar + filename;

        try (FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {
            byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];

            int readCount;
            int readCountTmp = 0;
            long startTime = System.currentTimeMillis();
            long startSubtime = startTime;
            long averageSpeed, instantSpeed;

            do {
                readCount = in.read(buffer);
                if (readCount == -1) continue;
                readCountTmp += readCount;
                recvSize += readCount;

                long curTime = System.currentTimeMillis();
                if (curTime - startSubtime > UPDATE_INFO_SECONDS * MILLISECONDS_IN_SECOND) {
                    startSubtime = curTime;

                    System.out.println("[" + filename + "]");

                    instantSpeed = readCountTmp / UPDATE_INFO_SECONDS;
                    System.out.println("\tInstant speed: " + instantSpeed + " B/s");

                    averageSpeed = recvSize / (curTime - startTime) * MILLISECONDS_IN_SECOND;
                    System.out.println("\tAverage speed: " + averageSpeed + " B/s");

                    readCountTmp = 0;
                }

                fileOutputStream.write(buffer, 0, readCount);
            } while (recvSize < fileSize);

            long curTime = System.currentTimeMillis() - startTime;
            averageSpeed = recvSize / curTime * MILLISECONDS_IN_SECOND;
            System.out.println("[" + filename + "] was uploaded");
            System.out.println("[" + filename + "] average speed: " + averageSpeed + " B/s");
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void sendResult(DataOutputStream out) throws IOException {
        if (recvSize != fileSize) {
            out.writeUTF(ProtocolMessage.FAILURE.toString());
        } else {
            out.writeUTF(ProtocolMessage.SUCCESS.toString());
        }
    }

    private static final int MILLISECONDS_IN_SECOND = 1000;
    private static final int UPDATE_INFO_SECONDS = 3;
}
