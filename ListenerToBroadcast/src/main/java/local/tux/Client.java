package local.tux;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class Client implements Comparable<Client> {


    ByteBuffer buffer = ByteBuffer.allocate(4096_000);
    SelectionKey key;
    SocketChannel socket;
    String ipAddress;
    String compKey;

    Stat stat = new Stat();

    private Client(String ipAddress, SocketChannel socket, SelectionKey key) {
        this.ipAddress = ipAddress;
        this.socket = socket;
        this.key = key;
        compKey = getRemoteAddressWithPort();
    }

    @SneakyThrows
    public static Client accept(SelectionKey key) {
        // 'Accept' selection keys contain a reference to the parent server-socket channel rather than their own socket
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();

        SocketChannel socket = channel.accept();

        String ipAddress = socket.socket().getInetAddress().getHostAddress();

        System.out.println("User connected " + ipAddress);
        socket.configureBlocking(false);

        // Let's also register this socket to our selector:
        // We are going to listen for two events (Read and Write).
        // These events tell us when the socket has bytes available to read, or if the buffer is available to write
        SelectionKey k = socket.register(key.selector(), SelectionKey.OP_WRITE);
        // We are only interested in events for write for our selector.
        k.interestOps(SelectionKey.OP_WRITE);

        // Here you can bind an object to the key as an attachment should you so desire.
        // This could be a reference to an object or anything else.
        var client = new Client(ipAddress, socket, k);
        k.attach(client);
        return client;
    }

    public Stat.InstantStat getState() {
        return stat.getStateSinceLastCalled();
    }

    public void disconnect() {
        try {
            socket.close();
            key.cancel();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void addMsg(ByteBuffer bytBuffer) throws InterruptedException {
        try {
            while (bytBuffer.hasRemaining()) {
                buffer.put(bytBuffer.get());
                stat.incrementReceived(1);
            }
        } catch (BufferOverflowException e) {
            log.error("Buffer capacity={}, position={}, limit={}", buffer.capacity(), buffer.position(), buffer.limit());
        }
    }

    public int send() throws IOException {

        buffer.flip();
        var currentBytesOut = socket.write(buffer);
        buffer.compact();
        stat.incrementSent(currentBytesOut);
        // If we weren't able to write the entire buffer out, make sure we alert the selector
        // so we can be notified when we are able to write more bytes to the socket
        return currentBytesOut;
    }

    @Override
    public int compareTo(Client o) {
        return compKey.compareTo(o.compKey);
    }

    private String getRemoteAddressWithPort() {
        var sock = socket.socket();
        return sock.getRemoteSocketAddress().toString();
    }
}
