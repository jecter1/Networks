package jecter.lab1;

public interface Constants {
    long SENDER_SLEEP_TIME_MS = 500;
    long DECREASER_SLEEP_TIME_MS = 1000;
    long MAX_TTL_MS = 3000;
    long MIN_TTL_MS = 0;

    int BUFFER_SIZE = 256;

    String DEFAULT_ADDRESS = "224.0.0.3";

    int RECEIVER_PORT = 2555;
    String SPECIAL_MESSAGE = "hello";
}