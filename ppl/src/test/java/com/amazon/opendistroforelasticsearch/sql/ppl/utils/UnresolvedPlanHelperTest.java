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

package com.amazon.opendistroforelasticsearch.sql.ppl.utils;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import java.util.Arrays;
import junit.framework.TestCase;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnresolvedPlanHelperTest extends TestCase {

  @Test
  public void addProjectForRenameOperator() {
    Rename rename = Mockito.mock(Rename.class);

    UnresolvedPlan plan = UnresolvedPlanHelper.addSelectAll(rename);
    assertTrue(plan instanceof Project);
  }

  @Test
  public void addProjectForProjectExcludeOperator() {
    Project project = Mockito.mock(Project.class);
    when(project.isExcluded()).thenReturn(true);

    UnresolvedPlan plan = UnresolvedPlanHelper.addSelectAll(project);
    assertTrue(plan instanceof Project);
    assertThat(((Project) plan).getProjectList(), Matchers.contains(AllFields.of()));
  }

  @Test
  public void dontAddProjectForProjectOperator() {
    Project project = Mockito.mock(Project.class);
    UnresolvedExpression expression = Mockito.mock(UnresolvedExpression.class);
    when(project.isExcluded()).thenReturn(false);
    when(project.getProjectList()).thenReturn(Arrays.asList(expression));

    UnresolvedPlan plan = UnresolvedPlanHelper.addSelectAll(project);
    assertTrue(plan instanceof Project);
    assertThat(((Project) plan).getProjectList(), Matchers.contains(expression));
  }
}