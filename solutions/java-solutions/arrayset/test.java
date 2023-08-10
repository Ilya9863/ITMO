/*
package info.kgeorgiy.ja.kurkin.arrayset;

import java.util.*;
    package info.kgeorgiy.ja.kurkin.arrayset;


import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {
    private final List<T> list;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this.list = new ArrayList<>();
        this.comparator = null;
    }

    public ArraySet(Comparator<? super T> comparator) {
        this.list = new ArrayList<>();
        this.comparator = comparator;
    }

    public ArraySet(Collection<T> list) {
        this.comparator = null;
        Set<T> current = new TreeSet<T>(list);
        this.list = List.copyOf(current);
    }

    boolean comp(T el1, T el2) {
        boolean result = false;
        if (comparator != null) {
            if (comparator.compare(el1, el2) == 0) {
                result = true;
            }
        } else {
            result = el1.equals(el2);
        }
        return result;
    }

    public ArraySet(Collection<T> list, Comparator<? super T> comparator) {
        this.list = new ArrayList<>();
        this.comparator = comparator;
        List<T> current = new ArrayList<>(list);
        current.sort(comparator);
        for (int i = 0; i < current.size(); i++) {
            if (i == 0) {
                this.list.add(current.get(0));
            } else {
                if (!comp(current.get(i - 1), current.get(i))) {
                    this.list.add(current.get(i));
                }
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }


    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        T tmp = (T) o;
        return Collections.binarySearch(list, tmp, comparator) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("dsfsd");
            }
        } else if (((Comparable<T>) fromElement).compareTo(toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        int posFrom = Collections.binarySearch(list, fromElement, comparator);
        int posTo = Collections.binarySearch(list, toElement, comparator);
        if (posTo < 0 && posFrom == posTo) {
            return new info.kgeorgiy.ja.kurkin.arrayset.ArraySet<>();
        }
        if (posFrom < 0) {
            posFrom = -posFrom - 1;
        }
        if (posTo < 0) {
            posTo = -posTo - 1;
        }
        return new info.kgeorgiy.ja.kurkin.arrayset.ArraySet<>(list.subList(posFrom, posTo));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        int pos = Collections.binarySearch(list, toElement, comparator);
        if (pos < 0) {
            pos = -pos - 1;
        }
        return new info.kgeorgiy.ja.kurkin.arrayset.ArraySet<>(list.subList(0, pos));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        int pos = Collections.binarySearch(list, fromElement, comparator);
        if (pos < 0) {
            pos = -pos - 1;
        }
        return new info.kgeorgiy.ja.kurkin.arrayset.ArraySet<>(list.subList(pos, list.size()));
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(size() - 1);
    }
    //reverseIterater DeshendigSet Lower Hier Siling Flor
}
*/
