package jecter.lab3.communication.exceptions;

public class CommunicationException extends Exception {
    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException() {
        super();
    }
}
