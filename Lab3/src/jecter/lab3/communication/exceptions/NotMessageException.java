package jecter.lab3.communication.exceptions;

public class NotMessageException extends CommunicationException {
    public NotMessageException(String message) {
        super(message);
    }

    public NotMessageException() {
        super();
    }
}
