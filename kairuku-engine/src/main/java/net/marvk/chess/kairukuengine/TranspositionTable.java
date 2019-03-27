package net.marvk.chess.kairukuengine;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

public class TranspositionTable<T> {
    private final LinkedHashMap<Long, T> table;
    private final int capacity;

    public TranspositionTable(final int capacity) {
        this.capacity = capacity;
        this.table = new LinkedHashMapWithCapacity<>(this.capacity);
    }

    public TranspositionTable() {
        this.capacity = -1;
        this.table = new LinkedHashMap<>();
    }

    public T get(final long hash) {
        return table.get(hash);
    }

    public T put(final long key, final T value) {
        return table.put(key, value);
    }

    public void clear() {
        table.clear();
    }

    @AllArgsConstructor
    @Data
    public static class Entry {
        private final ValuedMove valuedMove;
        private int depth;
        private int value;
        private NodeType nodeType;
    }

    public enum NodeType {
        EXACT,
        LOWERBOUND,
        UPPERBOUND
    }

    public double load() {
        if (capacity <= 0) {
            return 0.;
        }

        return ((double) table.size()) / capacity;
    }

    public int size() {
        return table.size();
    }

    private static class LinkedHashMapWithCapacity<T> extends LinkedHashMap<Long, T> {
        private final int capacity;

        LinkedHashMapWithCapacity(final int capacity) {
            super();
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, T> eldest) {
            return size() > capacity;
        }
    }
}
