package info.kgeorgiy.ja.kurkin.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final Deque<Runnable> myQueue;
    private final Thread[] threads;

    /**
     * default constructor with zero argument
     */
    public ParallelMapperImpl() {
        this.myQueue = new ArrayDeque<>();
        this.threads = new Thread[0];
    }

    /**
     * default constructor with one argument
     *
     * @param threads this param points number of threads to complete the task
     */
    public ParallelMapperImpl(int threads) {
        this.myQueue = new ArrayDeque<>();
        this.threads = new Thread[threads];
        start();
    }

//    /**
//     * default constructor with one argument
//     * Maps function {@code f} over specified {@code args}.
//     * Mapping for each element performed in parallel.
//     *
//     * @param parallelMapper argument from tests
//     */

//    public ParallelMapperImpl(ParallelMapper parallelMapper) {
//        // :NOTE: почему такая инициализация, а не данные из переменной?
//        this.myQueue = new ArrayDeque<>();
//        this.threads = new Thread[0];
//    }

    private void start() {
        for (int i = 0; i < threads.length; i++) {
            this.threads[i] = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (myQueue) {
                            while (myQueue.isEmpty()) {
                                myQueue.wait();
                            }
                            task = myQueue.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            this.threads[i].start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @param f function for application on elements
     * @param args elements to which we want to apply the f
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultHandler<R> resultHandler = new ResultHandler<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            synchronized (myQueue) {
                int ind = i;
                myQueue.add(() -> resultHandler.addTask(ind, f.apply(args.get(ind))));
                myQueue.notify();
            }
        }
        return resultHandler.getResults();
    }

    /**
     * Stops all threads. All unfinished mappings are left in undefined state.
     */
    @Override
    public void close() {
        Arrays.stream(threads).forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) { // :NOTE: ignored вместо e
            }
        }
    }

    private static class ResultHandler<R> {
        private final List<R> results;
        private int index = 0;

        ResultHandler(int size) {
            results = new ArrayList<>(Collections.nCopies(size, null));
        }

        public synchronized void addTask(int index, R res) {
            results.set(index, res);
            this.index++;
            if (this.index == results.size()) {
                notify();
            }
        }

        public synchronized List<R> getResults() throws InterruptedException {
            while (index < results.size()) {
                wait();
            }
            return results;
        }
    }

}