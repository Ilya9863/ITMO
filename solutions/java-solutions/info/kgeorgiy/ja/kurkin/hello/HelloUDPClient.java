package info.kgeorgiy.ja.kurkin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Kurkin Ilya
 * Relise interface {@link HelloClient}
 */

public class HelloUDPClient implements HelloClient {

    private ExecutorService threadsPull;
    final private int TIMER_FOR_ANSWER = 1000;

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
        genMain(HelloUDPClient::new).accept(args);
    }

    public static Consumer<String[]> genMain(Supplier<HelloClient> supplier) {
        return args -> {
            if (args == null || args.length != 5) {
                System.err.println("Not correct data, use this format input data: ");
                System.err.println("[the name or ip address][the port number to send requests to][request prefix (string)]" +
                        "[number of parallel request streams][the number of requests in each thread]");
            } else {
                String nameOrIpAddress = args[0];
                int portNumber = Integer.parseInt(args[1]);
                String requestPrefix = args[2];
                int numberOfThreads = Integer.parseInt(args[3]);
                int numberOfRequests = Integer.parseInt(args[4]);
                HelloClient client = supplier.get();
                client.run(nameOrIpAddress, portNumber, requestPrefix, numberOfThreads, numberOfRequests);
            }
        };
    }


    private String messageFormation(String prefix, int numberOfThread, int itForRequests, DatagramPacket packOfUser) {
        String message = prefix + numberOfThread + "_" + itForRequests;
        byte[] messageForSend = message.getBytes(StandardCharsets.UTF_8);
        int sizeMessageForSend = messageForSend.length;
        packOfUser.setData(messageForSend, 0, sizeMessageForSend);
        return message;
    }

    private boolean checkingAnswerOfServer(DatagramPacket packOfServer, String message) {
        String answerOfServer = new String(packOfServer.getData(), packOfServer.getOffset(),
                packOfServer.getLength(), StandardCharsets.UTF_8);
        if (answerOfServer.contains(message)) {
            System.out.println(answerOfServer);
            return true;
        }
        return false;
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
        threadsPull = Executors.newFixedThreadPool(threads);
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        for (int i = 0; i < threads; i++) {
            final int numberOfThread = i;
            Runnable messagePull = () -> {
                byte[] userPack = prefix.getBytes(StandardCharsets.UTF_8);
                byte[] serverPack;
                DatagramPacket packOfUser = new DatagramPacket(userPack, userPack.length, serverAddress);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket();
                    socket.setSoTimeout(TIMER_FOR_ANSWER);
                    serverPack = new byte[socket.getReceiveBufferSize()];
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
                DatagramPacket packOfServer = new DatagramPacket(serverPack, serverPack.length);
                for (int idForRequest = 0; idForRequest < requests; idForRequest++) {
                    for (; ; ) {
                        String message = messageFormation(prefix, numberOfThread + 1, idForRequest + 1, packOfUser);
                        try {
                            socket.send(packOfUser);
                            socket.receive(packOfServer);
                        } catch (IOException e) {
                            System.err.println("Failed send package " + e);
                        }
                        if (checkingAnswerOfServer(packOfServer, message)) break;
                    }
                }
                socket.close();
            };
            threadsPull.submit(messagePull);
        }
        close();
        try {
            threadsPull.awaitTermination(TIMER_FOR_ANSWER, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Can't wait : " + e);
        }
    }

    private void close() {
        threadsPull.shutdown();
        try {
            if (!threadsPull.awaitTermination(TIMER_FOR_ANSWER, TimeUnit.MILLISECONDS)) {
                System.err.println("Problems with stop working process ");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
