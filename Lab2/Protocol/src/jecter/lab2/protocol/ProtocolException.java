package jecter.lab2.protocol;

public class ProtocolException extends Exception {
    @Override
    public String toString() {
        return EXCEPTION_STRING;
    }

    private static final String EXCEPTION_STRING = "Protocol was violated by another participant";
}
