package info.kgeorgiy.ja.kurkin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Kurkin Ilya
 * Relise interface {@link HelloServer}
 */

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threadsPull;
    private ExecutorService threadOfMain;

    private final int TIMER_FOR_SHUT_DOWN = 1000;

    /**
     * @param args {@code args[0]} - number of port (int)
     *             {@code args[1]} - number of threads (int)
     */

    public static void main(String[] args) {
        genMainServer(HelloUDPServer::new).accept(args);
    }

    public static Consumer<String[]> genMainServer(Supplier<HelloServer> supplier) {
        return args -> {
            if (args == null || args.length != 2) {
                System.err.println("Not correct data, use this format input data: ");
                System.err.println("[port][number of threads]");
            } else {
                int port = Integer.parseInt(args[0]);
                int threads = Integer.parseInt(args[1]);
                try (HelloUDPServer server = new HelloUDPServer()) {
                    server.start(port, threads);
                    Scanner scanner = new Scanner(System.in);
                    scanner.next();
                }
            }
        };
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
            socket = new DatagramSocket(port);
            threadsPull = Executors.newFixedThreadPool(threads);
            threadOfMain = Executors.newSingleThreadExecutor();
        } catch (SocketException e) {
            System.err.println("Problems with starting working socket " + e);
        }
        Runnable server = () -> {
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data = new byte[socket.getReceiveBufferSize()];
                    DatagramPacket pack = new DatagramPacket(data, data.length);
                    socket.receive(pack);
                    Runnable answer = () -> {
                        String message = new String(pack.getData(), pack.getOffset(),
                                pack.getLength(), StandardCharsets.UTF_8);
                        String backMessage = "Hello, " + message;
                        pack.setData(backMessage.getBytes(StandardCharsets.UTF_8));
                        try {
                            socket.send(pack);
                        } catch (IOException e) {
                            System.err.println("Error sending message: " + e);
                        }
                    };
                    threadsPull.submit(answer);
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error with send message " + e);
                    }
                }
            }
        };
        threadOfMain.submit(server);
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        closeThreads(threadsPull, threadOfMain, TIMER_FOR_SHUT_DOWN);
    }

    static void closeThreads(ExecutorService threadsPull, ExecutorService threadOfMain, int timer_for_shut_down) {
        threadsPull.shutdown();
        threadOfMain.shutdown();
        try {
            if (!threadsPull.awaitTermination(timer_for_shut_down, TimeUnit.MILLISECONDS) ||
                    !threadOfMain.awaitTermination(timer_for_shut_down, TimeUnit.MILLISECONDS)) {
                System.err.println("Problems with stop working process ");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
