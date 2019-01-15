package org.nlpcn.es4sql.query.planner.physical.node.join;

import org.nlpcn.es4sql.query.planner.physical.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List implementation to avoid normal hash table degrading into linked list.
 */
public class ListHashTable<T> implements HashTable<T> {

    private List<Row<T>> rows = new ArrayList<>();

    @Override
    public void add(Row<T> row) {
        rows.add(row);
    }

    @Override
    public Collection<Row<T>> match(Row<T> row) {
        return rows;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Collection<Object>>[] rightFieldWithLeftValues() {
        return new Map[]{ new HashMap() };
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public void clear() {
        rows.clear();
    }
}
