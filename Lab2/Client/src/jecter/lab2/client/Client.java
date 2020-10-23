package jecter.lab2.client;

import jecter.lab2.protocol.ProtocolConstants;
import jecter.lab2.protocol.ProtocolException;
import jecter.lab2.protocol.ProtocolMessage;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length != ARGC_REQUIRED) {
            System.out.println(USAGE_STRING);
            return;
        }

        String filepath = args[ARGV_PATH_INDEX];

        if (filepath.getBytes().length > ProtocolConstants.FILEPATH_MAX_LENGTH_BYTES) {
            System.err.println(FILEPATH_TOO_BIG);
            return;
        }

        if (new File(filepath).length() > ProtocolConstants.FILE_MAX_SIZE_BYTES) {
            System.err.println(FILE_TOO_BIG);
            return;
        }

        String serverAddr = args[ARGV_ADDR_INDEX];
        int    serverPort = Integer.parseInt(args[ARGV_PORT_INDEX]);

        try (Socket clientSocket  = new Socket(serverAddr, serverPort);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream  in  = new DataInputStream(clientSocket.getInputStream())){
            System.out.println("Connected");

            protocolSwap(in, out, ProtocolMessage.INFO);
            sendInfo(out, filepath);

            protocolSwap(in, out, ProtocolMessage.FILE);
            sendFile(out, filepath);

            recvResult(in);

        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ProtocolException exc) {
            System.out.println(exc.toString());
        }
    }

    private static void protocolSwap(DataInputStream in, DataOutputStream out, ProtocolMessage message)
                                                                            throws ProtocolException, IOException {
        String serverMessage = in.readUTF();
        if (!serverMessage.equals(message.toString())) {
            throw new ProtocolException();
        }
        out.writeUTF(message.toString());
    }

    private static void recvResult(DataInputStream in) throws ProtocolException, IOException {
        String result = in.readUTF();
        if (result.equals(ProtocolMessage.SUCCESS.toString())) {
            System.out.println(SUCCESS_STRING);
        } else if (result.equals(ProtocolMessage.FAILURE.toString())) {
            System.out.println(FAILURE_STRING);
        } else {
            throw new ProtocolException();
        }
    }

    private static void sendInfo(DataOutputStream out, String filepath) throws IOException {
        int firstFilenameIndex = filepath.lastIndexOf(File.separatorChar) + 1;
        String filename = filepath.substring(firstFilenameIndex);
        out.writeUTF(filename);

        long fileSize = new File(filepath).length();
        out.writeLong(fileSize);

        out.flush();
    }

    private static void sendFile(DataOutputStream out, String filepath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
            byte[] buffer = new byte[ProtocolConstants.BUFFER_SIZE];

            int readCount;
            while (-1 != (readCount = fileInputStream.read(buffer))) {
                out.write(buffer, 0, readCount);
            }

            out.flush();
        }
    }

    private static final int ARGC_REQUIRED = 3;
    private static final int ARGV_PATH_INDEX = 0;
    private static final int ARGV_ADDR_INDEX = 1;
    private static final int ARGV_PORT_INDEX = 2;

    private static final String USAGE_STRING = "Usage: java -jar Client.jar <filepath> <address> <port>";

    private static final String FILEPATH_TOO_BIG = "Error: filepath is too big";
    private static final String FILE_TOO_BIG = "Error: file is too big";

    private static final String SUCCESS_STRING = "File was uploaded successfully";
    private static final String FAILURE_STRING = "Whoops! File wasn't uploaded";
}