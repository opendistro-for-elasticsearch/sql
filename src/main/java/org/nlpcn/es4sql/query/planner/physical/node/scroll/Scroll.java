package org.nlpcn.es4sql.query.planner.physical.node.scroll;

import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.join.TableInJoinRequestBuilder;
import org.nlpcn.es4sql.query.maker.QueryMaker;
import org.nlpcn.es4sql.query.planner.core.ExecuteParams;
import org.nlpcn.es4sql.query.planner.core.PlanNode;
import org.nlpcn.es4sql.query.planner.physical.Row;
import org.nlpcn.es4sql.query.planner.physical.estimation.Cost;
import org.nlpcn.es4sql.query.planner.physical.node.BatchPhysicalOperator;
import org.nlpcn.es4sql.query.planner.resource.ResourceManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.CLIENT;
import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.EXTRA_QUERY_FILTER;
import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.RESOURCE_MANAGER;
import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.TIMEOUT;

/**
 * ES Scroll API as physical implementation of TableScan
 */
public class Scroll extends BatchPhysicalOperator<SearchHit> {

    /** Request to submit to ES to scroll over */
    private final TableInJoinRequestBuilder request;

    /** Page size to scroll over index */
    private final int pageSize;

    /** Client connection to ElasticSearch */
    private Client client;

    /** Currently undergoing Scroll */
    private SearchResponse scrollResponse;

    /** Time out */
    private Integer timeout;

    /** Resource monitor manager */
    private ResourceManager resourceMgr;


    public Scroll(TableInJoinRequestBuilder request, int pageSize) {
        this.request = request;
        this.pageSize = pageSize;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[0];
    }

    @Override
    public Cost estimate() {
        return new Cost();
    }

    @Override
    public void open(ExecuteParams params) throws Exception {
        super.open(params);
        client = params.get(CLIENT);
        timeout = params.get(TIMEOUT);
        resourceMgr = params.get(RESOURCE_MANAGER);

        Object filter = params.get(EXTRA_QUERY_FILTER);
        if (filter instanceof BoolQueryBuilder) {
            request.getRequestBuilder().setQuery(
                generateNewQueryWithExtraFilter((BoolQueryBuilder) filter));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Received extra query filter, re-build query: {}", Strings.toString(
                    request.getRequestBuilder().request().source(), true, true
                ));
            }
        }
    }

    @Override
    public void close() {
        if (scrollResponse != null) {
            LOG.debug("Closing all scroll resources");
            ClearScrollResponse clearScrollResponse = client.prepareClearScroll().
                                                             addScrollId(scrollResponse.getScrollId()).
                                                             get();
            if (!clearScrollResponse.isSucceeded()) {
                LOG.warn("Failed to close scroll: {}", clearScrollResponse.status());
            }
            scrollResponse = null;
        } else {
            LOG.debug("Scroll already be closed");
        }
    }

    @Override
    protected Collection<Row<SearchHit>> prefetch() {
        Objects.requireNonNull(client, "Client connection is not ready");
        Objects.requireNonNull(resourceMgr, "ResourceManager is not set");
        Objects.requireNonNull(timeout, "Time out is not set");

        if (scrollResponse == null) {
            loadFirstBatch();
            updateMetaResult();
        } else {
            loadNextBatchByScrollId();
        }
        return wrapRowForCurrentBatch();
    }

    /**
     * Extra filter pushed down from upstream. Re-parse WHERE clause with extra filter
     * because ES RequestBuilder doesn't allow QueryBuilder inside be changed after added.
     */
    private QueryBuilder generateNewQueryWithExtraFilter(BoolQueryBuilder filter) throws SqlParseException {
        Where where = request.getOriginalSelect().getWhere();
        BoolQueryBuilder newQuery;
        if (where != null) {
            newQuery = QueryMaker.explan(where, false);
            newQuery.must(filter);
        } else {
            newQuery = filter;
        }
        return newQuery;
    }

    private void loadFirstBatch() {
        scrollResponse = request.getRequestBuilder().
                                 addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC).
                                 setSize(pageSize).
                                 setScroll(TimeValue.timeValueSeconds(timeout)).
                                 get();
    }

    private void updateMetaResult() {
        resourceMgr.getMetaResult().addTotalNumOfShards(scrollResponse.getTotalShards());
        resourceMgr.getMetaResult().addSuccessfulShards(scrollResponse.getSuccessfulShards());
        resourceMgr.getMetaResult().addFailedShards(scrollResponse.getFailedShards());
        resourceMgr.getMetaResult().updateTimeOut(scrollResponse.isTimedOut());
    }

    private void loadNextBatchByScrollId() {
        scrollResponse = client.prepareSearchScroll(scrollResponse.getScrollId()).
                                setScroll(TimeValue.timeValueSeconds(timeout)).
                                get();
    }

    @SuppressWarnings("unchecked")
    private Collection<Row<SearchHit>> wrapRowForCurrentBatch() {
        SearchHit[] hits = scrollResponse.getHits().getHits();
        Row[] rows = new Row[hits.length];
        for (int i = 0; i < hits.length; i++) {
            rows[i] = new SearchHitRow(hits[i], request.getAlias());
        }
        return Arrays.asList(rows);
    }

    @Override
    public String toString() {
        return "Scroll [ " + describeTable() + ", pageSize=" + pageSize + " ]";
    }

    private String describeTable() {
        return request.getOriginalSelect().getFrom().get(0).getIndex() + " as " + request.getAlias();
    }


    /*********************************************
     *          Getters for Explain
     *********************************************/

    public String getRequest() {
        return Strings.toString(request.getRequestBuilder().request().source());
    }
}
