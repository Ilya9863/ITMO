package info.kgeorgiy.ja.kurkin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private int startingPositionInTheBlock(int optimizationOfLastBlock, int loopsPerThread, int iterate) {
        return Math.min(iterate, optimizationOfLastBlock) * (loopsPerThread + 1) +
                Math.max(0, iterate - optimizationOfLastBlock) * loopsPerThread;
    }

    private int theFinalPositionInTheBlock(int start, int optimizationOfLastBlock, int loopsPerThread, int iterate) {
        return start + loopsPerThread + (iterate < optimizationOfLastBlock ? 1 : 0);
    }

    private <T, E> List<Stream<? extends T>> workingWithThreads(int amountOfThreads, int optimizationOfLastBlock, int loopsPerThread,
                                                                List<? extends T> values) {
        List<Stream<? extends T>> listValues = new ArrayList<>();
        for (int i = 0; i < amountOfThreads; i++) {
            final int start = startingPositionInTheBlock(optimizationOfLastBlock, loopsPerThread, i);
            final int end = theFinalPositionInTheBlock(start, optimizationOfLastBlock, loopsPerThread, i);
            listValues.add(values.subList(start, end).stream());
        }
        return listValues;
    }

    private void close(Thread[] threads) throws InterruptedException {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Arrays.stream(threads).forEach(Thread::interrupt);
                throw e;
            }
        }
    }

    private <T, E> E startingParallel(final int arrivingAmountOfThreads, final List<? extends T> values,
                                      final Function<Stream<? extends T>, E> task,
                                      final Function<Stream<E>, E> ansCollector)
            throws InterruptedException {
        if (arrivingAmountOfThreads <= 0 || values.size() == 0) {
            throw new InterruptedException("Can't starting parallel working");
        }
        // :NOTE: а если список пустой? будет деление на 0
        int amountOfThreads = Math.min(arrivingAmountOfThreads, values.size());
        int loopsPerThread = values.size() / amountOfThreads;
        int optimizationOfLastBlock = values.size() % amountOfThreads;
        // :NOTE: просто List слева
        List<E> answers = new ArrayList<>(Collections.nCopies(amountOfThreads, null));
        if (mapper != null) {
            List<Stream<? extends T>> listValues = workingWithThreads(amountOfThreads, optimizationOfLastBlock,
                    loopsPerThread, values);
            List<E> ans = mapper.map(task, listValues);
            return ansCollector.apply(ans.stream());
        } else {
            List<Stream<? extends T>> listValues =
                    workingWithThreads(amountOfThreads, optimizationOfLastBlock,
                            loopsPerThread, values);
            Thread[] threads = new Thread[amountOfThreads];
            for (int i = 0; i < listValues.size(); i++) {
                final int idx = i;
                threads[i] = new Thread(() -> answers.set(idx, task.apply(listValues.get(idx))));
                threads[i].start();
            }
            close(threads);
            return ansCollector.apply(answers.stream());
        }
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return startingParallel(threads, values,
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: выразить через максимум
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return startingParallel(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: выразить через any
        return !all(threads, values, predicate.negate());
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return Math.toIntExact(startingParallel(threads, values,
                stream -> stream.filter(predicate).count(),
                stream -> stream.reduce(0L, Long::sum)));
    }

    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return list of joined results of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return startingParallel(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicate. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return startingParallel(threads, values,
                stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values  values to map.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return startingParallel(threads, values,
                stream -> stream.map(f).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }
}