package org.nlpcn.es4sql.query.planner.physical.node.join;

import com.google.common.collect.Sets;
import org.nlpcn.es4sql.query.planner.physical.Row;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.nlpcn.es4sql.query.planner.logical.node.Join.JoinCondition;

/**
 * Hash table group with each hash table per AND join condition.
 */
public class HashTableGroup<T> implements HashTable<T> {

    private final HashTable<T>[] hashTables;

    /** Number of rows stored in the hash table (in other words, = block size) */
    private int numOfRows = 0;

    @SuppressWarnings("unchecked")
    public HashTableGroup(JoinCondition condition) {
        int groupSize = condition.groupSize();
        if (groupSize == 0) {
            // Create one hash table (degraded to list) for Cross Join
            hashTables = new HashTable[]{ new ListHashTable() };
        }
        else {
            hashTables = new HashTable[groupSize];
            for (int i = 0; i < groupSize; i++) {
                hashTables[i] = new DefaultHashTable<>(
                    condition.leftColumnNames(i),
                    condition.rightColumnNames(i)
                );
            }
        }
    }

    @Override
    public void add(Row<T> row) {
        for (HashTable<T> hashTable : hashTables) {
            hashTable.add(row);
        }
        numOfRows++;
    }

    @Override
    public Collection<Row<T>> match(Row<T> row) {
        Set<Row<T>> allMatched = Sets.newIdentityHashSet();
        for (HashTable<T> hashTable : hashTables) {
            allMatched.addAll(hashTable.match(row));
        }
        return allMatched;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Collection<Object>>[] rightFieldWithLeftValues() {
        return Arrays.stream(hashTables).
                      map(hashTable -> hashTable.rightFieldWithLeftValues()[0]). // Make interface consistent
                      toArray(Map[]::new);
    }

    @Override
    public boolean isEmpty() {
        return numOfRows == 0;
    }

    @Override
    public int size() {
        return numOfRows;
    }

    @Override
    public void clear() {
        for (HashTable<T> hashTable : hashTables) {
            hashTable.clear();
        }
        numOfRows = 0;
    }

}
