/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.planner;

import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Visitor that explains a physical plan to JSON format.
 */
public class Explain extends PhysicalPlanNodeVisitor<JsonNode, Object> {

  private final ObjectMapper mapper = new ObjectMapper();

  public String explain(PhysicalPlan plan) {
    return plan.accept(this, null).toPrettyString();
  }

  @Override
  public JsonNode visitProject(ProjectOperator node, Object context) {
    ObjectNode json = mapper.createObjectNode();
    ArrayNode array = json.putArray("fields");
    node.getProjectList().forEach(array::addPOJO);
    return null;
  }

  @Override
  public JsonNode visitFilter(FilterOperator node, Object context) {
    ObjectNode json = mapper.createObjectNode();
    return addChildren(json, node);
  }

  @Override
  public JsonNode visitTableScan(TableScanOperator node, Object context) {
    ObjectNode json = mapper.createObjectNode();
    return addChildren(json, node);
  }

  private ObjectNode addChildren(ObjectNode json, PhysicalPlan node) {
    ArrayNode array = json.putArray("children");
    node.getChild().forEach(child -> array.add(child.accept(this, null)));
    return json;
  }

}
