package jecter.lab1;

import jecter.lab1.threads.Decreaser;
import jecter.lab1.threads.Receiver;
import jecter.lab1.threads.Sender;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main implements Constants {
    public static void main(String[] args) {
        String groupIP;

        if (args.length == ARGC_WITH_ADDRESS) {
            groupIP = args[ARGV_ADDRESS_INDEX];
        } else if (args.length == ARGC_WITHOUT_ADDRESS) {
            groupIP = DEFAULT_ADDRESS;
        } else {
            System.out.println(USAGE_STRING);
            return;
        }

        InetAddress group;
        try {
            group = InetAddress.getByName(groupIP);
        } catch (UnknownHostException exc) {
            exc.printStackTrace();
            return;
        }

        TimeTable timeTable = new TimeTable();

        Receiver  receiver  = new Receiver(timeTable, group);
        Decreaser decreaser = new Decreaser(timeTable);
        Sender    sender    = new Sender(group);
    }

    private static final int ARGV_ADDRESS_INDEX = 0;

    private static final int ARGC_WITH_ADDRESS = 1;
    private static final int ARGC_WITHOUT_ADDRESS = 0;

    private static final String USAGE_STRING = "Usage: java " + Main.class.getName() + " <Address> (optional)";
}
