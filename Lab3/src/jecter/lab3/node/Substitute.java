package jecter.lab3.node;

import jecter.lab3.communication.Addressable;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Substitute implements Addressable, Serializable {
    private static final InetSocketAddress NO_ADDRESS = null;


    private InetSocketAddress address;


    public Substitute(Addressable addressable) {
        this.address = addressable.getAddress();
    }

    public Substitute() {
        this.address = NO_ADDRESS;
    }

    public boolean exists() {
        return address != NO_ADDRESS;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public void setAddress(Addressable addressable) {
        this.address = addressable.getAddress();
    }

    public void remove() {
        address = NO_ADDRESS;
    }

    @Override
    public InetSocketAddress getAddress() {
        if (exists()) {
            return address;
        } else {
            throw new RuntimeException();
        }
    }
}
