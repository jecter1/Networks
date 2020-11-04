package jecter.lab3.node;

import jecter.lab3.communication.Addressable;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Neighbour implements Addressable {
    private static final String DEFAULT_NAME = "Unknown";
    private static final long RESPONDING_TIME_MS = 1000;


    private final InetSocketAddress address;
    private final String name;
    private long lastReceivedPingMs;
    private final Substitute substitute = new Substitute();


    public Neighbour(Addressable addressable, String name) {
        this.address = addressable.getAddress();
        this.name = name;
        updateTime();
    }

    public Neighbour(Addressable addressable) {
        this(addressable, DEFAULT_NAME);
    }

    public void updateTime() {
        lastReceivedPingMs = System.currentTimeMillis();;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    public Substitute getSubstitute() {
        return substitute;
    }

    public String getName() {
        return name;
    }

    public boolean isResponding() {
        return (System.currentTimeMillis() - lastReceivedPingMs) < RESPONDING_TIME_MS;
    }

    public void setSubstitute(Substitute substitute) {
        this.substitute.setAddress(substitute);
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
            Neighbour neighbour = (Neighbour) object;
            return Objects.equals(address, neighbour.address);
        } else {
            return false;
        }
    }

    private boolean hasSameClass(Object object) {
        return (object != null && getClass() == object.getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
