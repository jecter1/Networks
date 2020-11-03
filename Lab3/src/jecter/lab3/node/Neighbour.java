package jecter.lab3.node;

import jecter.lab3.communication.Addressable;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Neighbour implements Addressable {
    private static final String DEFAULT_NAME = "Unknown";


    private final InetSocketAddress address;
    private String name;


    public Neighbour(Addressable addressable, String name) {
        this.address = addressable.getAddress();
        this.name = name;
    }

    public Neighbour(Addressable addressable) {
        this(addressable, DEFAULT_NAME);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return !name.equals(DEFAULT_NAME);
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    public String getName() {
        return name;
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
