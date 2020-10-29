package jecter.lab3;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        if (args.length != ARGC_WITH_PARENT && args.length != ARGC_WITHOUT_PARENT) {
            System.out.println(USAGE_STR);
            return;
        }

        String name = args[ARGV_INDEX_NAME];
        int port;
        int loss;
        Node node;

        try {
            port = Integer.parseInt(args[ARGV_INDEX_PORT]);
            loss = Integer.parseInt(args[ARGV_INDEX_LOSS]);
            node = new Node(name, port, loss);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (args.length == ARGC_WITH_PARENT) {
            InetAddress cnip;
            int cnpt;

            try {
                cnip = InetAddress.getByName(args[ARGV_INDEX_CNIP]);
                cnpt = Integer.parseInt(args[ARGV_INDEX_CNPT]);
                node.connect(cnip, cnpt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        node.startCommunication();
    }

    private static final int ARGV_INDEX_NAME = 0;
    private static final int ARGV_INDEX_PORT = 1;
    private static final int ARGV_INDEX_LOSS = 2;
    private static final int ARGV_INDEX_CNIP = 3;
    private static final int ARGV_INDEX_CNPT = 4;

    private static final int ARGC_WITHOUT_PARENT = 3;
    private static final int ARGC_WITH_PARENT = 5;

    private static final String USAGE_STR = "Usage: java -jar Chat.jar <NAME> <PORT> <LOSS> [CNIP] [CNPT]\n" +
                                            "------------------------------------------------------------\n" +
                                            "NAME - node's own name\n" +
                                            "PORT - node's own port\n" +
                                            "LOSS - percent of packages that node will ignore, " +
                                                   "positive integer less than 100\n" +
                                            "CNIP - (optional) parent's IP address\n" +
                                            "CNPT - (optional) parent's port\n" +
                                            "------------------------------------------------------------\n" +
                                            "Use CNIP & CNPT together or don't use at all";
}
