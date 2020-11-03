package jecter.lab3.application;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Arguments {
    private static String[] args;

    private static final InetSocketAddress DETACHED_PARENT_ADDRESS = null;

    private static final int LENGTH_DETACHED = 3;
    private static final int LENGTH_ATTACHED = 5;

    private static final int NAME_INDEX = 0;
    private static final int PORT_INDEX = 1;
    private static final int LOSS_INDEX = 2;
    private static final int PARENT_IP_INDEX = 3;
    private static final int PARENT_PORT_INDEX = 4;

    private static final int MIN_LOSS = 0;
    private static final int MAX_LOSS = 100;

    private static final String HAS_NO_PARENT = "There is no parentSocketAddress in arguments";
    private static final String USAGE = "Usage:\n" +
                                        "\tjava -jar Chat.jar <name> <port> <loss>\n" +
                                        "or\n" +
                                        "\tjava -jar Chat.jar <name> <port> <loss> <parentIP> <parentPort>";


    private final String name;
    private final int lossPercent;
    private final SocketAddress address;
    private InetSocketAddress parentAddress;


    public static Arguments parse(String[] args) {
        Arguments.args = args;
        try {
            return tryToParse();
        } catch (Exception e) {
            throw new RuntimeException(USAGE);
        }
    }

    private static Arguments tryToParse() {
        if (isNodeDetached()) {
            return parseDetached();
        } else if (isNodeAttached()) {
            return parseAttached();
        } else {
            throw new RuntimeException();
        }
    }

    private static boolean isNodeDetached() {
        return args.length == LENGTH_DETACHED;
    }

    private static Arguments parseDetached() {
        try {
            return tryToParseDetached();
        } catch (Exception exc) {
            throw new RuntimeException();
        }
    }

    private static Arguments tryToParseDetached() {
        String name = tryToParseName();
        int lossPercent = tryToParseLossPercent();
        SocketAddress address = tryToParseAddress();
        return new Arguments(name, lossPercent, address);
    }

    private static String tryToParseName() {
        String name = args[NAME_INDEX];
        if (isCorrectName(name)) {
            return name;
        } else {
            throw new RuntimeException();
        }
    }

    private static boolean isCorrectName(String name) {
        return true;
    }

    private static int tryToParseLossPercent() {
        int lossPercent = Integer.parseInt(args[LOSS_INDEX]);
        if (isCorrectLossPercent(lossPercent)) {
            return lossPercent;
        } else {
            throw new RuntimeException();
        }
    }

    private static boolean isCorrectLossPercent(int lossPercent) {
        return (lossPercent >= MIN_LOSS && lossPercent <= MAX_LOSS);
    }

    private static SocketAddress tryToParseAddress() {
        int port = tryToParsePort();

        return new InetSocketAddress(port);
    }

    private static int tryToParsePort() {
        return Integer.parseInt(args[PORT_INDEX]);
    }

    private static boolean isNodeAttached() {
        return args.length == LENGTH_ATTACHED;
    }

    private static Arguments parseAttached() {
        Arguments nodeArguments = parseDetached();
        try {
            nodeArguments.parentAddress = tryToParseParentAddress();
            return nodeArguments;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static InetSocketAddress tryToParseParentAddress() {
        InetAddress parentIP = tryToParseParentIP();
        int parentPort = tryToParseParentPort();

        return new InetSocketAddress(parentIP, parentPort);
    }

    private static InetAddress tryToParseParentIP() {
        try {
            return InetAddress.getByName(args[PARENT_IP_INDEX]);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static int tryToParseParentPort() {
        return Integer.parseInt(args[PARENT_PORT_INDEX]);
    }


    private Arguments(String name, int lossPercent, SocketAddress address, InetSocketAddress parentAddress) {
        this.name = name;
        this.lossPercent = lossPercent;
        this.address = address;
        this.parentAddress = parentAddress;
    }

    private Arguments(String name, int lossPercent, SocketAddress address) {
        this(name, lossPercent, address, DETACHED_PARENT_ADDRESS);
    }

    public String getName() {
        return name;
    }

    public int getLossPercent() {
        return lossPercent;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public InetSocketAddress getParentAddress() {
        if (hasParent()) {
            return parentAddress;
        } else {
            throw new RuntimeException(HAS_NO_PARENT);
        }
    }

    private boolean hasParent() {
        return (parentAddress != DETACHED_PARENT_ADDRESS);
    }
}
