package info.kgeorgiy.ja.kurkin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author Kurkin Ilya
 * Relise interface {@link HelloUDPNonblockingClient}
 */


public class HelloUDPNonblockingClient implements HelloClient {

    private static final int TIME_OUT = 111;

    /**
     * Starting from the console to send a message to the server
     *
     * @param args {@code args[0]} - the name or ip address (String)
     *             {@code args[1]} - the port number to send requests to (int)
     *             {@code args[2]} - request prefix (String)
     *             {@code args[3]} - number of parallel request streams (int)
     *             {@code args[4]} - the number of requests in each thread (int)
     */


    public static void main(String[] args) {
        HelloUDPClient.genMain(HelloUDPNonblockingClient::new).accept(args);
    }

    private static class Data {
        private final int threadId;
        private int requestId;
        ByteBuffer bufferSize;
        private final String prefix;

        private Data(int threadId, int requestId, String prefix, int bufferSize) {
            this.threadId = threadId;
            this.requestId = requestId;
            this.prefix = prefix;
            this.bufferSize = ByteBuffer.allocate(bufferSize);
        }
    }


    private void checkingSelectedKeys(Selector selector) {
        if (selector.selectedKeys().isEmpty()) {
            for (var it : selector.keys()) {
                it.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    private String sendMessageToServer(String prefix, Data data) {
        return prefix + data.threadId + '_' + data.requestId;
    }

    private void configureDatagramChannel(int threads, SocketAddress socketAddress, Selector selector, String prefix) {
        for (int i = 0; i < threads; i++) {
            try {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(socketAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE,
                        new Data(i + 1, 1, prefix, datagramChannel.socket().getReceiveBufferSize()));
            } catch (IOException e) {
                System.err.println("Problems with open channel " + e);
            }
        }
    }

    private void isWritable(String prefix, Data data, DatagramChannel datagramChannel,
                            SocketAddress socketAddress, SelectionKey keyBySelector) throws IOException {
        String message = sendMessageToServer(prefix, data);
        datagramChannel.send(ByteBuffer.wrap(
                message.getBytes(StandardCharsets.UTF_8)
        ), socketAddress);
        keyBySelector.interestOps(SelectionKey.OP_READ);
    }

    private void isReadable(DatagramChannel datagramChannel, Data data,
                            String prefix, SelectionKey keyBySelector, int requests) throws IOException {
        datagramChannel.receive(data.bufferSize.clear());
        String message = sendMessageToServer(prefix, data);
        String answerByServer = StandardCharsets
                .UTF_8
                .decode(data.bufferSize.flip())
                .toString();
        if (answerByServer.contains(message)) {
            System.out.println(answerByServer);
            data.requestId++;
        }
        keyBySelector.interestOps(SelectionKey.OP_WRITE);
        if (requests < data.requestId) {
            datagramChannel.close();
        }
    }

    /**
     * Runs Hello client.
     * This method should return when all requests are completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress socketAddress;
        Selector selector;
        try {
            selector = Selector.open();
            socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
            configureDatagramChannel(threads, socketAddress, selector, prefix);
            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                selector.select(TIME_OUT);
                checkingSelectedKeys(selector);
                var it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    try {
                        SelectionKey keyBySelector = it.next();
                        DatagramChannel datagramChannel = (DatagramChannel) keyBySelector.channel();
                        Data data = (Data) keyBySelector.attachment();
                        if (keyBySelector.isWritable()) {
                            isWritable(prefix, data, datagramChannel,
                                    socketAddress, keyBySelector);
                        } else if (keyBySelector.isReadable()) {
                            isReadable(datagramChannel, data,
                                    prefix, keyBySelector, requests);
                        }
                    } finally {
                        it.remove();
                    }
                }
            }
            selector.close();
        } catch (IOException e) {
            System.err.println("Can't connect to the host " + e);
        }
    }
}