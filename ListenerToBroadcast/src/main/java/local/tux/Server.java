package local.tux;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class Server {

    private static final int TIMEOUT = 3000;
    private static final Set<Client> clients = new ConcurrentSkipListSet<>();


    public static DatagramChannel startUdpServer(int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", port);
        DatagramChannel server = ChannelBuilder.createDatagramChannel(address, false);
        log.info("Server started at {}", address);
        return server;
    }

    public static ServerSocketChannel startTcpServer(int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", port);
        var server = ChannelBuilder.createServerSocketChannel(address, false);
        log.info("Server started at {}", address);
        return server;
    }

    @SneakyThrows
    public static void start() {

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    clients.forEach(c -> {
                        var stat = c.getState();
                        log.info("compKey={}, received={}, send={}, delta={}, buffer={}, r={}B/s, s={}B/s",
                            c.compKey, stat.getBytesReceived(), stat.getBytesSent(), stat.delta(), stat.getBytesInBuffer(), stat.getBytesReceived() / 3, stat.getBytesSent() / 3
                        );
                    });
                } catch (Exception e) {
                }
            }
        }).start();

        var udpServer1 = Server.startUdpServer(7355);
        var tcpServer2 = Server.startTcpServer(7366);
        Selector selector = Selector.open();

        udpServer1.register(selector, SelectionKey.OP_WRITE);
        udpServer1.register(selector, SelectionKey.OP_READ);

        tcpServer2.register(selector, SelectionKey.OP_ACCEPT);
        var received = ByteBuffer.allocate(200);
        while (true) {

            if (selector.select(TIMEOUT) == 0) {
                System.out.print(".");
                continue;
            }

            var keys = selector.selectedKeys();

            var iterator = keys.iterator();

            while (iterator.hasNext()) {
                var key = iterator.next();


                if (key.isValid() == false) {
                    continue;
                }
                var channel = (AbstractSelectableChannel) key.channel();
                if (key.isAcceptable()) {
                    accept(key);
                }
                if (key.isWritable()) {
                    if (key.attachment() != null) {

                        var client = (Client) key.attachment();
                        if (client != null) {
                            try {
                                client.send();
                            } catch (Exception e) {
                                log.warn("not able to send to client. disconnect this client");
                                ((Client) key.attachment()).disconnect();
                                client.disconnect();
                                clients.remove(client);
                            }
                        }
                    }

                } else if (key.isReadable()) {
                    //received
                    if (channel instanceof DatagramChannel) {
                        var ch = (DatagramChannel) channel;
                        try {
                            var size = handleRead(ch, received);
                            if (size > 0) {
                                for (var client : clients) {
                                    client.addMsg(received.slice(0, size));
                                }
                            } else {
                                log.info("received 0 byte");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            received.clear();
                        }
                    }
                }
                iterator.remove();
            }
        }
    }

    private static void accept(SelectionKey key) throws IOException {
        clients.add(Client.accept(key));
    }

    public static int handleRead(DatagramChannel channel, ByteBuffer bufferIn) throws IOException {
        int bytesIn = 0;
        channel.receive(bufferIn);

        bytesIn = bufferIn.position();
        if (bytesIn == -1) {
            throw new IOException("Socket closed");
        }
        return bytesIn;
    }

}
