package org.nlpcn.es4sql.query.planner.physical.node.join;

import org.nlpcn.es4sql.query.planner.physical.Row;

import java.util.Collection;
import java.util.Map;

/**
 * Hash table interface
 *
 * @param <T>  data object type
 */
public interface HashTable<T> {

    /**
     * Add one row to the hash table
     * @param row   row
     */
    void add(Row<T> row);


    /**
     * Find all matched row(s) in the hash table.
     * @param row   row to be matched
     * @return      all matches
     */
    Collection<Row<T>> match(Row<T> row);


    /**
     * Mapping from right field to value(s) of left size
     * @return
     */
    Map<String, Collection<Object>>[] rightFieldWithLeftValues();


    /**
     * Get size of hash table
     * @return  size of hash table
     */
    int size();


    /**
     * Is hash table empty?
     * @return  true for yes
     */
    boolean isEmpty();


    /**
     * Clear internal data structure
     */
    void clear();

}
