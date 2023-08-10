package info.kgeorgiy.ja.kurkin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.*;


/**
 * @author Kurkin Ilya
 * Relise interface {@link HelloUDPNonblockingServer}
 */

public class HelloUDPNonblockingServer implements HelloServer {
    private final int TIME_OUT = 111;

    private static final int TIMER_FOR_SHUT_DOWN = 10;
    private Selector selector;

    private DatagramChannel datagramChannel;

    private ExecutorService threadOfMain;

    private ConcurrentLinkedDeque<Data> pullTasks;

    private int bufferSize;

    /**
     * @param args {@code args[0]} - number of port (int)
     *             {@code args[1]} - number of threads (int)
     */


    public static void main(String[] args) {
        HelloUDPServer.genMainServer(HelloUDPNonblockingServer::new).accept(args);
    }

    private static class Data {

        private static final int CONSTANT_START_LENGTH_MESSAGE = 7;
        private final SocketAddress clientAddress;
        private final String message;
        ByteBuffer bufferForMessage;

        Data(SocketAddress clientAddress, String message, ByteBuffer bufferSize) {
            this.clientAddress = clientAddress;
            this.message = message;
            this.bufferForMessage = ByteBuffer.allocate(message.length() + CONSTANT_START_LENGTH_MESSAGE);
        }
    }

    private void initialisationDatagramChannel(int port) throws IOException {
        selector = Selector.open();
        datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(new InetSocketAddress(port));
        bufferSize = datagramChannel.socket().getReceiveBufferSize();
        datagramChannel.register(selector, SelectionKey.OP_READ);
        threadOfMain = Executors.newSingleThreadExecutor();
        pullTasks = new ConcurrentLinkedDeque<>();
    }

    private boolean checkPullTask(SelectionKey keyBySelector) {
        if (pullTasks.isEmpty()) {
            keyBySelector.interestOps(SelectionKey.OP_READ);
            return true;
        }
        return false;
    }

    private void isWritable(SelectionKey keyBySelector) {
        Data data = pullTasks.pollFirst();
        ByteBuffer bufferForMessage = ByteBuffer.wrap(data != null ? data.message.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        try {
            datagramChannel.send(bufferForMessage, data != null ? data.clientAddress : null);
        } catch (IOException e) {
            System.err.println("Problems with buffer " + e);
        }
        keyBySelector.interestOpsOr(SelectionKey.OP_READ);
    }

    private void isReadable(SelectionKey keyBySelector) throws IOException {
        ByteBuffer bufferForMessage = ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize());
        SocketAddress address = datagramChannel.receive(bufferForMessage);
        bufferForMessage.flip();
        String mes = StandardCharsets.UTF_8.decode(bufferForMessage).toString();
        pullTasks.offer(new Data(address, "Hello, " + mes, bufferForMessage));
        keyBySelector.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */

    @Override
    public void start(int port, int threads) {
        try {
            initialisationDatagramChannel(port);
            Runnable server = () -> {
                while (!Thread.interrupted() && !datagramChannel.socket().isClosed()) {
                    try {
                        selector.select(TIME_OUT);
                        var it = selector.selectedKeys().iterator();
                        while (it.hasNext()) {
                            try {
                                SelectionKey keyBySelector = it.next();
                                if (keyBySelector.isWritable() && !checkPullTask(keyBySelector)) {
                                    isWritable(keyBySelector);
                                }
                                if (keyBySelector.isReadable()) {
                                    isReadable(keyBySelector);
                                }
                            } catch (IOException e) {
                                System.err.println("Problems with send answer " + e);
                                close();
                            } finally {
                                it.remove();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Problems with Runnable server " + e);
                    }
                }
            };
            threadOfMain.submit(server);
        } catch (IOException e) {
            System.err.println("Problems with starting Server " + e);
        }

    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        try {
            selector.close();
        } catch (IOException e) {
            System.err.println("Problems with close Selector " + e);
        }
        try {
            datagramChannel.close();
        } catch (IOException e) {
            System.err.println("Problems with close DatagramChannel " + e);
        }
        HelloUDPServer.closeThreads(threadOfMain, threadOfMain, TIMER_FOR_SHUT_DOWN);
    }

}