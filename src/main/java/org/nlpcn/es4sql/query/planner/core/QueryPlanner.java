package org.nlpcn.es4sql.query.planner.core;

import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.MetaSearchResult;
import org.elasticsearch.search.SearchHit;
import org.nlpcn.es4sql.query.planner.explain.Explanation;
import org.nlpcn.es4sql.query.planner.explain.JsonExplanationFormat;
import org.nlpcn.es4sql.query.planner.logical.LogicalPlan;
import org.nlpcn.es4sql.query.planner.physical.PhysicalPlan;
import org.nlpcn.es4sql.query.planner.resource.ResourceManager;
import org.nlpcn.es4sql.query.planner.resource.Stats;

import java.util.List;

import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.CLIENT;
import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.RESOURCE_MANAGER;
import static org.nlpcn.es4sql.query.planner.core.ExecuteParams.ExecuteParamType.TIMEOUT;

/**
 * Query planner that driver the logical planning, physical planning, execute and explain.
 */
public class QueryPlanner {

    /** Connection to ElasticSearch */
    private final Client client;

    /** Query plan configuration */
    private final Config config;

    /** Optimized logical plan */
    private final LogicalPlan logicalPlan;

    /** Best physical plan to execute */
    private final PhysicalPlan physicalPlan;

    /** Statistics collector */
    private Stats stats;

    /** Resource monitor and statistics manager */
    private ResourceManager resourceMgr;


    public QueryPlanner(Client client, Config config, QueryParams params) {
        this.client = client;
        this.config =  config;
        this.stats = new Stats(client);
        this.resourceMgr = new ResourceManager(stats, config);

        logicalPlan = new LogicalPlan(config, params);
        logicalPlan.optimize();

        physicalPlan = new PhysicalPlan(logicalPlan);
        physicalPlan.optimize();
    }

    /**
     * Execute query plan
     * @return  response of the execution
     */
    public List<SearchHit> execute() {
        ExecuteParams params = new ExecuteParams();
        params.add(CLIENT, client);
        params.add(TIMEOUT, config.timeout());
        params.add(RESOURCE_MANAGER, resourceMgr);
        return physicalPlan.execute(params);
    }

    /**
     * Explain query plan
     * @return  explanation string of the plan
     */
    public String explain() {
        return new Explanation(
            logicalPlan, physicalPlan,
            new JsonExplanationFormat(4)
        ).toString();
    }

    public MetaSearchResult getMetaResult() {
        return resourceMgr.getMetaResult();
    }

    /** Setter for unit test */
    public void setStats(Stats stats) {
        this.stats = stats;
        this.resourceMgr = new ResourceManager(stats, config);
    }
}
