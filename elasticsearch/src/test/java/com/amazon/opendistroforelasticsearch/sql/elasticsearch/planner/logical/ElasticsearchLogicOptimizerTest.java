/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.utils.Utils.indexScan;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.utils.Utils.indexScanAgg;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.limit;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.sort;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.LogicalPlanOptimizer;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


class ElasticsearchLogicOptimizerTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  /**
   * SELECT intV as i FROM schema WHERE intV = 1.
   */
  @Test
  void project_filter_merge_with_relation() {
    assertEquals(
        project(
            indexScan("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))),
            DSL.named("i", DSL.ref("intV", INTEGER))
        ),
        optimize(
            project(
                filter(
                    relation("schema"),
                    dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                ),
                DSL.named("i", DSL.ref("intV", INTEGER)))
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema GROUP BY string_value.
   */
  @Test
  void aggregation_merge_relation() {
    assertEquals(
        project(
            indexScanAgg("schema", ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    relation("schema"),
                    ImmutableList
                        .of(DSL.named("AVG(intV)",
                            dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("longV",
                        dsl.abs(DSL.ref("longV", LONG))))),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema WHERE intV = 1 GROUP BY string_value.
   */
  @Test
  void aggregation_merge_filter_relation() {
    assertEquals(
        project(
            indexScanAgg("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
                ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    filter(
                        relation("schema"),
                        dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                    ),
                    ImmutableList
                        .of(DSL.named("AVG(intV)",
                            dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("longV",
                        dsl.abs(DSL.ref("longV", LONG))))),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }

  @Disabled
  @Test
  void aggregation_cant_merge_indexScan_with_project() {
    assertEquals(
        aggregation(
            ElasticsearchLogicalIndexScan.builder().relationName("schema")
                .filter(dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))))
                .projectList(Collections.singletonList(DSL.named("i", DSL.ref("intV", INTEGER))))
                .build(),
            ImmutableList
                .of(DSL.named("AVG(intV)",
                    dsl.avg(DSL.ref("intV", INTEGER)))),
            ImmutableList.of(DSL.named("longV",
                dsl.abs(DSL.ref("longV", LONG))))),
        optimize(
            aggregation(
                ElasticsearchLogicalIndexScan.builder().relationName("schema")
                    .filter(dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))))
                    .projectList(
                        Collections.singletonList(DSL.named("i", DSL.ref("intV", INTEGER))))
                    .build(),
                ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))))
    );
  }

  /**
   * Sort - Relation --> IndexScan.
   */
  @Test
  void sort_merge_with_relation() {
    assertEquals(
        indexScan("schema", Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("intV", INTEGER))),
        optimize(
            sort(
                relation("schema"),
                Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("intV", INTEGER))
            )
        )
    );
  }

  /**
   * Sort - IndexScan --> IndexScan.
   */
  @Test
  void sort_merge_with_indexScan() {
    assertEquals(
        indexScan("schema",
            Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("intV", INTEGER)),
            Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))),
        optimize(
            sort(
                indexScan("schema", Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("intV", INTEGER))),
                Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
            )
        )
    );
  }

  /**
   * Sort - Filter - Relation --> IndexScan.
   */
  @Test
  void sort_filter_merge_with_relation() {
    assertEquals(
        indexScan("schema",
            dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
            Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
        ),
        optimize(
            sort(
                filter(
                    relation("schema"),
                    dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                ),
                Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
            )
        )
    );
  }

  @Test
  void sort_with_expression_cannot_merge_with_relation() {
    assertEquals(
        sort(
            relation("schema"),
            Pair.of(Sort.SortOption.DEFAULT_ASC, dsl.abs(DSL.ref("intV", INTEGER)))
        ),
        optimize(
            sort(
                relation("schema"),
                Pair.of(Sort.SortOption.DEFAULT_ASC, dsl.abs(DSL.ref("intV", INTEGER)))
            )
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema GROUP BY stringV ORDER BY stringV.
   */
  @Test
  void sort_merge_indexagg() {
    assertEquals(
        project(
            indexScanAgg("schema",
                ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING))),
                ImmutableList
                    .of(Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("stringV", STRING)))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                sort(
                    aggregation(
                        relation("schema"),
                        ImmutableList
                            .of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                        ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
                    Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("stringV", STRING))
                ),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema GROUP BY stringV ORDER BY stringV.
   */
  @Test
  void sort_merge_indexagg_nulls_last() {
    assertEquals(
        project(
            indexScanAgg("schema",
                ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING))),
                ImmutableList
                    .of(Pair.of(Sort.SortOption.DEFAULT_DESC, DSL.ref("stringV", STRING)))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                sort(
                    aggregation(
                        relation("schema"),
                        ImmutableList
                            .of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                        ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
                    Pair.of(Sort.SortOption.DEFAULT_DESC, DSL.ref("stringV", STRING))
                ),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }


  /**
   * Can't Optimize the following query.
   * SELECT avg(intV) FROM schema GROUP BY stringV ORDER BY avg(intV).
   */
  @Test
  void sort_refer_to_aggregator_should_not_merge_with_indexAgg() {
    assertEquals(
        sort(
            indexScanAgg("schema",
                ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
            Pair.of(Sort.SortOption.DEFAULT_DESC, DSL.ref("AVG(intV)", INTEGER))
        ),
        optimize(
            sort(
                indexScanAgg("schema",
                    ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
                Pair.of(Sort.SortOption.DEFAULT_DESC, DSL.ref("AVG(intV)", INTEGER))
            )
        )
    );
  }

  /**
   * Can't Optimize the following query.
   * SELECT avg(intV) FROM schema GROUP BY stringV ORDER BY stringV ASC NULL_LAST.
   */
  @Test
  void sort_with_customized_option_should_not_merge_with_indexAgg() {
    assertEquals(
        sort(
            indexScanAgg("schema",
                ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
            Pair.of(new Sort.SortOption(Sort.SortOrder.ASC, Sort.NullOrder.NULL_LAST),
                DSL.ref("stringV", STRING))
        ),
        optimize(
            sort(
                indexScanAgg("schema",
                    ImmutableList.of(DSL.named("AVG(intV)", dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("stringV", DSL.ref("stringV", STRING)))),
                Pair.of(new Sort.SortOption(Sort.SortOrder.ASC, Sort.NullOrder.NULL_LAST),
                    DSL.ref("stringV", STRING))
            )
        )
    );
  }

  @Test
  void limit_merge_with_relation() {
    assertEquals(
        project(
            indexScan("schema", 1, 1),
            DSL.named("intV", DSL.ref("intV", INTEGER))
        ),
        optimize(
            project(
                limit(
                    relation("schema"),
                    1, 1
                ),
                DSL.named("intV", DSL.ref("intV", INTEGER))
            )
        )
    );
  }

  @Test
  void limit_merge_with_index_scan() {
    assertEquals(
        project(
            indexScan("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
                1, 1
            ),
            DSL.named("intV", DSL.ref("intV", INTEGER))
        ),
        optimize(
            project(
                limit(
                    filter(
                        relation("schema"),
                        dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                    ), 1, 1
                ),
            DSL.named("intV", DSL.ref("intV", INTEGER)))
        )
    );
  }

  @Test
  void limit_merge_with_index_scan_sort() {
    assertEquals(
        project(
            indexScan("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
                1, 1,
                Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
            ),
            DSL.named("intV", DSL.ref("intV", INTEGER))
        ),
        optimize(
            project(
                limit(
                    sort(
                        filter(
                            relation("schema"),
                            dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                        ),
                        Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
                    ), 1, 1
                ),
                DSL.named("intV", DSL.ref("intV", INTEGER))
            )
        )
    );
  }

  @Test
  void aggregation_cant_merge_index_scan_with_limit() {
    assertEquals(
        project(
            aggregation(
                indexScan("schema", 10, 0),
                ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    indexScan("schema", 10, 0),
                    ImmutableList
                        .of(DSL.named("AVG(intV)",
                            dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("longV",
                        dsl.abs(DSL.ref("longV", LONG))))),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))));
  }

  private LogicalPlan optimize(LogicalPlan plan) {
    final LogicalPlanOptimizer optimizer = ElasticsearchLogicalPlanOptimizerFactory.create();
    final LogicalPlan optimize = optimizer.optimize(plan);
    return optimize;
  }
}