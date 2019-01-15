package org.nlpcn.es4sql.query.planner.logical.node;

import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.query.join.TableInJoinRequestBuilder;
import org.nlpcn.es4sql.query.planner.core.PlanNode;
import org.nlpcn.es4sql.query.planner.logical.LogicalOperator;
import org.nlpcn.es4sql.query.planner.physical.PhysicalOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Selection expression
 */
public class Filter implements LogicalOperator {

    private final LogicalOperator next;

    /** Alias to WHERE clause mapping */
    private final Map<String, Where> aliasWhereMap = new HashMap<>();

    public Filter(LogicalOperator next, List<TableInJoinRequestBuilder> tables) {
        this.next = next;

        for (TableInJoinRequestBuilder table : tables) {
            Select select = table.getOriginalSelect();
            if (select.getWhere() != null) {
                aliasWhereMap.put(table.getAlias(), select.getWhere());
            }
        }
    }

    public Filter(LogicalOperator next) {
        this.next = next;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{ next };
    }

    @Override
    public boolean isNoOp() {
        return aliasWhereMap.isEmpty();
    }

    @Override
    public <T> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<T>> optimalOps) {
        return new PhysicalOperator[]{ optimalOps.get(next) }; // Always no-op after push down, skip it by returning next
    }

    public void pushDown(String tableAlias, Filter pushedDownFilter) {
        Where pushedDownWhere = pushedDownFilter.aliasWhereMap.remove(tableAlias);
        if (pushedDownWhere != null) {
            aliasWhereMap.put(tableAlias, pushedDownWhere);
        }
    }

    @Override
    public String toString() {
        return "Filter [ conditions=" + aliasWhereMap.values() + " ]";
    }

}
