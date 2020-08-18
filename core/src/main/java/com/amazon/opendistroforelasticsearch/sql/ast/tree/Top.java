package com.amazon.opendistroforelasticsearch.sql.ast.tree;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AST node represent Top operation.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Top extends UnresolvedPlan {

  private UnresolvedPlan child;
  private final List<Argument> noOfResults;
  private final List<Field> fields;
  private final List<UnresolvedExpression> groupExprList;

  /**
   * Top Constructors.
   */
  public Top(
      List<Argument> noOfResults, List<Field> fields, List<UnresolvedExpression> groupExprList) {
    this.noOfResults = noOfResults;
    this.fields = fields;
    this.groupExprList = groupExprList;
  }

  @Override
  public Top attach(UnresolvedPlan child) {
    this.child = child;
    return this;
  }

  @Override
  public List<UnresolvedPlan> getChild() {
    return ImmutableList.of(this.child);
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitTop(this, context);
  }
}

