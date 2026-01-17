package com.scarasol.itemrarity.data;

import com.scarasol.itemrarity.api.rarity.RarityData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Scarasol
 */
public class RarityDataRegister<T extends RarityData> implements Iterable<T> {

    private final Deque<T> deque = new ArrayDeque<>();
    private final int index;
    private final boolean fifo;

    public RarityDataRegister(int index, boolean fifo) {
        this.index = index;
        this.fifo = fifo;
    }

    @Override
    public Iterator<T> iterator() {
        return fifo ? deque.descendingIterator() : deque.iterator();
    }

    public void register(T value) {
        deque.push(value);
    }

    public int getIndex() {
        return index;
    }

    public boolean contains(T value) {
        return deque.contains(value);
    }

    public Stream<T> stream() {
        return deque.stream();
    }

    public void clear() {
        deque.clear();
    }
}
