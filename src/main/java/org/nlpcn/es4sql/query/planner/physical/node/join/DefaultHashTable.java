package org.nlpcn.es4sql.query.planner.physical.node.join;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nlpcn.es4sql.query.planner.physical.Row;
import org.nlpcn.es4sql.query.planner.physical.Row.RowKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.emptyList;

/**
 * Hash table implementation.
 * In the case of no join condition, hash table degrades to linked list with all rows in block paired to RowKey.NULL
 *
 * @param <T>   Row data type
 */
public class DefaultHashTable<T> implements HashTable<T> {

    private static final Logger LOG = LogManager.getLogger();

    /** Hash table implementation */
    private final Multimap<RowKey, Row<T>> table = ArrayListMultimap.create();

    /** Left join conditions to generate key to build hash table by left rows from block */
    private final String[] leftJoinFields;

    /** Right join conditions to generate key to probe hash table by right rows */
    private final String[] rightJoinFields;


    public DefaultHashTable(String[] leftJoinFields, String[] rightJoinFields) {
        this.leftJoinFields = leftJoinFields;
        this.rightJoinFields = rightJoinFields;
    }

    /**
     * Add row in block to hash table by left conditions in ON.
     * For the duplicate key, append them to the list in value (MultiMap)
     */
    @Override
    public void add(Row<T> row) {
        RowKey key = row.key(leftJoinFields);
        if (key == RowKey.NULL) {
            LOG.debug("Skip rows with NULL column value during build: row={}, conditions={}", row, leftJoinFields);
        }
        else {
            table.put(key, row);
        }
    }

    /** Probe hash table to match right rows by values of right conditions */
    @Override
    public Collection<Row<T>> match(Row<T> row) {
        RowKey key = row.key(rightJoinFields);
        if (key == RowKey.NULL) {
            LOG.debug("Skip rows with NULL column value during probing: row={}, conditions={}", row, rightJoinFields);
            return emptyList();
        }
        return table.get(key); // Multimap returns empty list rather null.
    }

    /** Right joined field name with according column value list to push down */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Collection<Object>>[] rightFieldWithLeftValues() {
        Map<String, Collection<Object>> result = new HashMap<>(); // Eliminate potential duplicate in values
        for (RowKey key : table.keySet()) {
            Object[] keys = key.keys();
            for (int i = 0; i < keys.length; i++) {
                result.computeIfAbsent(rightJoinFields[i], (k -> new HashSet<>())).
                       add(lowercaseIfStr(keys[i])); // Terms stored in lower case in ES
            }
        }

        // Convert value of Map from Guava's Set to JDK list which is expected by ES writer
        for (Entry<String, Collection<Object>> entry : result.entrySet()) {
            entry.setValue(new ArrayList<>(entry.getValue()));
        }
        return new Map[]{ result };
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

    @Override
    public void clear() {
        table.clear();
    }

    private Object lowercaseIfStr(Object key) {
        return key instanceof String ? ((String) key).toLowerCase() : key;
    }

}
