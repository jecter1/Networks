package jecter.lab2.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        if (args.length != ARGC_REQUIRED) {
            System.out.println(USAGE_STRING);
            return;
        }

        int serverPort = Integer.parseInt(args[ARGV_PORT_INDEX]);

        File dir = new File(UPLOADS_DIRECTORY);
        if (!dir.exists() && !dir.mkdir()) {
            System.err.println("Couldn't create dir \"" + UPLOADS_DIRECTORY + "\" here");
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(8);

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new ClientHandler(clientSocket));
            }
            executorService.shutdown();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    static final String UPLOADS_DIRECTORY = "uploads";

    private static final int ARGC_REQUIRED = 1;
    private static final int ARGV_PORT_INDEX = 0;

    private static final String USAGE_STRING = "Usage: java -jar Server.jar <port>";
}
