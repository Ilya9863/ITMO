package info.kgeorgiy.ja.kurkin.concurrent;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        Random random = new Random();
        ArrayList<Integer> list = new ArrayList<>();
        // size of the list
        int size = 22;

        // add random numbers to the list
        for (int i = 0; i < size; i++) {
            // generate a random number between 0 and 99
            int randomNumber = random.nextInt(100);
            list.add(randomNumber);
            System.out.print(list.get(i) + " ");
        }
        System.out.println();
        // number of loops and threads
        int n = 5;
        // number of loops per thread
        int loopsPerThread = list.size() / n;
        ArrayList<Integer> answers = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            answers.add(0);
        }
        // create n threads to process n loops
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            final int start = i * loopsPerThread;
            final int end = start + loopsPerThread + (i == n - 1 ?  list.size() % n : 0);
            //System.out.println(start + " " + end);
            final int index = i;
         //   threads[i] = new Thread(() -> answers.set(index, new MyRunnable(start, end, list).getMinimum()));
            threads[i].start();
        }

        // wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int result = answers.get(0);

        for (Integer answer : answers) {
            System.out.print(answer + " ");
            result = Math.max(result, answer);
        }
        System.out.println();
        System.out.println(result);
    }
}
public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> a =  List.of(0,0,0,0,0,0,-1);
        List<Integer> b =  List.of(0,0,0,0,0,0,1);
        ScalarIP s = new IterativeParallelism();
        System.out.println(s.maximum(8,b, Comparator.naturalOrder()));
        System.out.println(s.minimum(8, a, Comparator.naturalOrder()));

    }
}