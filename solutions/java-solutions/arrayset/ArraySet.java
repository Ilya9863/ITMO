package arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private final List<T> list;
    private final Comparator<? super T> comparator;
    private NavigableSet<T> reverseSet = null;

    public ArraySet() {
        this(new ArrayList<>(), null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(new ArrayList<>(), comparator);
    }

    public ArraySet(Collection<T> list) {
        this(list, null);
    }

    private ArraySet(List<T> list, Comparator<? super T> comp) {
        this.list = list;
        comparator = comp;
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

    private T checkerLowerAndFloor(int pos) {
        if (pos < 0) {
            return null;
        }
        return list.get(Math.abs(pos));
    }

    private int lowerBound(T t, boolean inclusive) {
        int pos = Collections.binarySearch(list, t, comparator);
        if (pos < 0) {
            return Math.abs(pos) - 2;
        }
        return inclusive ? pos : pos - 1;
    }

    //<
    @Override
    public T lower(T t) {
        int pos = lowerBound(t, false);
        return checkerLowerAndFloor(pos);
    }

    //<=
    @Override
    public T floor(T t) {
        int pos = lowerBound(t, true);
        return checkerLowerAndFloor(pos);
    }

    private T checkerCeilingAndHigher(int pos) {
        if (pos >= size()) {
            return null;
        } else {
            return list.get(pos);
        }
    }

    private int upperBound(T t, boolean inclusive) {
        int pos = Collections.binarySearch(list, t, comparator);
        if (pos < 0) {
            return -pos - 1;
        }
        return inclusive ? pos : pos + 1;
    }

    //>=
    @Override
    public T ceiling(T t) {
        int pos = upperBound(t, true);
        return checkerCeilingAndHigher(pos);
    }

    //>
    @Override
    public T higher(T t) {
        int pos = upperBound(t, false);
        return checkerCeilingAndHigher(pos);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        NavigableSet<T> rev = reverseSet;
        if (rev == null) {
            return reverseSet = new ArraySet<>(new ReverseSet<>(list), Collections.reverseOrder(comparator));
        } else return rev;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("not comparable");
            }
        } else {
            if (((Comparable<T>) fromElement).compareTo(toElement) > 0) {
                throw new IllegalArgumentException("fromElement > toElement");
            }
        }
        int from = lowerBound(fromElement, !fromInclusive) + 1;
        int to = lowerBound(toElement, toInclusive) + 1;
        return new ArraySet<>(list.subList(from, to), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int pos = lowerBound(toElement, inclusive) + 1;
        return new ArraySet<>(list.subList(0, pos), comparator);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        int pos = lowerBound(fromElement, !inclusive) + 1;
        return new ArraySet<>(list.subList(pos, size()), comparator);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        T tmp = (T) o;
        return Collections.binarySearch(list, tmp, comparator) >= 0;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
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

    @Override
    public int size() {
        return list.size();
    }

    private static class ReverseSet<T> extends AbstractList<T> {
        private final List<T> reverseList;

        private ReverseSet(List<T> reverseList) {
            this.reverseList = List.copyOf(reverseList);
        }

        @Override
        public T get(int index) {
            return reverseList.get(size() - index - 1);
        }

        @Override
        public int size() {
            return reverseList.size();
        }
    }
}