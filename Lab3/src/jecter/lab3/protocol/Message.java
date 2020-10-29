package jecter.lab3.protocol;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    UUID id;
    MessageHeader header;
    String senderName;
    String text;

    public Message(UUID id, MessageHeader header, String senderName, String text) {
        this.id = id;
        this.header = header;
        this.senderName = senderName;
        this.text = text;
    }

    public UUID getID() {
        return id;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return senderName;
    }
}
