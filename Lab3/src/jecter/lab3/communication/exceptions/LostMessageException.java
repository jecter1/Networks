package jecter.lab3.communication.exceptions;

public class LostMessageException extends CommunicationException {
    public LostMessageException(String message) {
        super(message);
    }

    public LostMessageException() {
        super();
    }
}
