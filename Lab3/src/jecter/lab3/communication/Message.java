package jecter.lab3.communication;

import jecter.lab3.node.Neighbour;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Message implements Serializable {
    public enum Header {
        REQUEST,
        TEXT,
        CONFIRMATION
    }


    private static final String EMPTY_TEXT = "";


    public final UUID id;
    public final Header header;
    public final String sourceName;
    public final String text;


    public Message(UUID id, Header header, String sourceName, String text) {
        this.id = id;
        this.header = header;
        this.sourceName = sourceName;
        this.text = text;
    }

    public Message(UUID id, Header header, String sourceName) {
        this(id, header, sourceName, EMPTY_TEXT);
    }

    public Message(Header header, String sourceName, String text) {
        this(UUID.randomUUID(), header, sourceName, text);
    }

    public Message(Header header, String sourceName) {
        this(header, sourceName, EMPTY_TEXT);
    }

    @Override
    public boolean equals(Object object) {
        if (sameRefs(object)) {
            return equalsForSameRefs(object);
        } else {
            return equalsForDifferentRefs(object);
        }
    }

    private boolean sameRefs(Object object) {
        return (this == object);
    }

    private boolean equalsForSameRefs(Object object) {
        return true;
    }

    private boolean equalsForDifferentRefs(Object object) {
        if (hasSameClass(object)) {
            Message message = (Message) object;
            return Objects.equals(id, message.id);
        } else {
            return false;
        }
    }

    private boolean hasSameClass(Object object) {
        return (object != null && getClass() == object.getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
