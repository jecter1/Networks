package jecter.lab3.communication;

import jecter.lab3.node.Substitute;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Message implements Serializable {
    public enum Header {
        REQUEST,
        TEXT,
        CONFIRMATION,
        PING
    }


    private static final String NO_TEXT = null;


    private final UUID id;
    private final Header header;
    private final String sourceName;
    private String text = NO_TEXT;
    private Substitute substitute = new Substitute();


    public Message(UUID id, Header header, String sourceName) {
        this.id = id;
        this.header = header;
        this.sourceName = sourceName;
    }

    public Message(Header header, String sourceName) {
        this(UUID.randomUUID(), header, sourceName);
    }

    public void addText(String text) {
        if (header.equals(Header.TEXT)) {
            this.text = text;
        } else {
            throw new RuntimeException();
        }
    }

    public void addSubstitute(Substitute substitute) {
        if (header.equals(Header.PING)) {
            this.substitute = substitute;
        } else {
            throw new RuntimeException();
        }
    }

    public UUID getID() {
        return id;
    }

    public Header getHeader() {
        return header;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getText() {
        if (text != NO_TEXT) {
            return text;
        } else {
            throw new RuntimeException();
        }
    }

    public Substitute getSubstitute() {
        return substitute;
    }

    public boolean is(Header header) {
        return (this.header.equals(header));
    }

    @Override
    public boolean equals(Object object) {
        if (sameRefs(object)) {
            return true;
        } else {
            return equalsForDifferentRefs(object);
        }
    }

    private boolean sameRefs(Object object) {
        return (this == object);
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
