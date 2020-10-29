package jecter.lab3;

import java.net.InetAddress;
import java.util.Objects;

public class Neighbour {
    private String name = DEFAULT_NAME;
    private final InetAddress address;
    private final int port;

    public Neighbour(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public Neighbour(InetAddress address, int port, String name) {
        this(address, port);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbour neighbour = (Neighbour) o;
        return port == neighbour.port && Objects.equals(address, neighbour.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, address);
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final String DEFAULT_NAME = "Unknown";
}
