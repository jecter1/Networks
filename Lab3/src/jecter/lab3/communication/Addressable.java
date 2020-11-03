package jecter.lab3.communication;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface Addressable {
    InetSocketAddress getAddress();
}
