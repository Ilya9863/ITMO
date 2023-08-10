package info.kgeorgiy.ja.kurkin.concurrent;

import java.util.ArrayList;

public class MyRunnable implements Runnable {
    private final int start;
    private final int end;

    private int min;

    private final ArrayList<Integer> list;

    public MyRunnable(int start, int end, ArrayList<Integer> list) {
        this.start = start;
        this.end = end;
        this.list = list;
    }

    public int getMinimum() {
        run();
        return min;
    }

    @Override
    public void run() {
        min = list.get(start);
        for (int i = start + 1; i < end; i++) {
            Math.min(min, list.get(i));
        }
    }
}