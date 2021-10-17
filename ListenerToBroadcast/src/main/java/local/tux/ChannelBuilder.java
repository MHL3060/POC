package local.tux;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;

/**
 * http://cs.baylor.edu/~donahoo/practical/JavaSockets2/code/UDPEchoServerSelector.java
 */
@Slf4j
public class ChannelBuilder {


    private static DatagramChannel openChannel(boolean isBlocking) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(isBlocking);
        return datagramChannel;
    }

    public static DatagramChannel createDatagramChannel(SocketAddress local, boolean isBlocking) throws IOException {
        return openChannel(isBlocking).bind(local);
    }

    public static ServerSocketChannel createServerSocketChannel(SocketAddress local, boolean isBlocking) throws IOException {
        var socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(isBlocking);
        socketChannel.bind(local);
        return socketChannel;
    }

}
