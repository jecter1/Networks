package jecter.lab1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class TimeTable implements Constants {
    private final Map<String, Long> mapTTL = new HashMap<>();

    public synchronized void updateTime(String ip) {
        if (!mapTTL.containsKey(ip)) {
            mapTTL.put(ip, MAX_TTL_MS);
            System.out.println(ADD_IP_STRING);
            print();
        } else {
            mapTTL.put(ip, MAX_TTL_MS);
        }
    }

    public synchronized void decreaseTime(long value) {
        if (mapTTL.isEmpty()) return;

        Iterator<String> ipIterator = mapTTL.keySet().iterator();

        while (ipIterator.hasNext()) {
            String ip = ipIterator.next();
            long TTL = mapTTL.get(ip);
            if (mapTTL.get(ip) - value < MIN_TTL_MS) {
                ipIterator.remove();
                System.out.println(REMOVE_IP_STRING);
                print();
            } else {
                mapTTL.replace(ip, TTL - value);
            }
        }
    }

    public synchronized void print() {
        int count = PRINT_FIRST_NUMBER;
        System.out.println(COUNTER_PREFIX + COUNT_INFO_PREFIX + mapTTL.size() + COUNTER_POSTFIX);
        for (var ip : mapTTL.keySet()) {
            System.out.println(count + NUMBER_INFO_SEPARATOR + ip);
            ++count;
        }
        System.out.println();
    }

    private static final String COUNTER_NAME = "cloneCount";
    private static final String COUNTER_PREFIX = "[";
    private static final String COUNTER_POSTFIX = "]";
    private static final String COUNT_INFO_PREFIX = COUNTER_NAME + " == ";
    private static final String ADD_IP_STRING = "++" + COUNTER_NAME;
    private static final String REMOVE_IP_STRING = "--" + COUNTER_NAME;

    private static final String NUMBER_INFO_SEPARATOR = ") ";
    private static final int PRINT_FIRST_NUMBER = 1;
}