/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.utils;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DedupCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.HeadCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.RareCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortFieldContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StatsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.TopCommandContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedArgument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;


/**
 * Util class to get all arguments as a list from the PPL command.
 */
public class ArgumentFactory {

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx FieldsCommandContext instance
   * @return the list of arguments fetched from the fields command
   */
  public static List<Argument> getArgumentList(FieldsCommandContext ctx) {
    return Collections.singletonList(
        ctx.MINUS() != null
            ? new Argument("exclude", new Literal(true, DataType.BOOLEAN))
            : new Argument("exclude", new Literal(false, DataType.BOOLEAN))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx StatsCommandContext instance
   * @return the list of arguments fetched from the stats command
   */
  public static List<Argument> getArgumentList(StatsCommandContext ctx) {
    return Arrays.asList(
        ctx.partitions != null
            ? new Argument("partitions", getArgumentValue(ctx.partitions))
            : new Argument("partitions", new Literal(1, DataType.INTEGER)),
        ctx.allnum != null
            ? new Argument("allnum", getArgumentValue(ctx.allnum))
            : new Argument("allnum", new Literal(false, DataType.BOOLEAN)),
        ctx.delim != null
            ? new Argument("delim", getArgumentValue(ctx.delim))
            : new Argument("delim", new Literal(" ", DataType.STRING)),
        ctx.dedupsplit != null
            ? new Argument("dedupsplit", getArgumentValue(ctx.dedupsplit))
            : new Argument("dedupsplit", new Literal(false, DataType.BOOLEAN))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx DedupCommandContext instance
   * @return the list of arguments fetched from the dedup command
   */
  public static List<Argument> getArgumentList(DedupCommandContext ctx) {
    return Arrays.asList(
        ctx.number != null
            ? new Argument("number", getArgumentValue(ctx.number))
            : new Argument("number", new Literal(1, DataType.INTEGER)),
        ctx.keepempty != null
            ? new Argument("keepempty", getArgumentValue(ctx.keepempty))
            : new Argument("keepempty", new Literal(false, DataType.BOOLEAN)),
        ctx.consecutive != null
            ? new Argument("consecutive", getArgumentValue(ctx.consecutive))
            : new Argument("consecutive", new Literal(false, DataType.BOOLEAN))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx HeadCommandContext instance
   * @return the list of arguments fetched from the head command
   */
  public static List<UnresolvedArgument> getArgumentList(HeadCommandContext ctx,
      UnresolvedExpression unresolvedExpr) {
    return Arrays.asList(
        ctx.keeplast != null
            ? new UnresolvedArgument("keeplast", getArgumentValue(ctx.keeplast))
            : new UnresolvedArgument("keeplast", new Literal(true, DataType.BOOLEAN)),
        ctx.whileExpr != null && unresolvedExpr != null
            ? new UnresolvedArgument("whileExpr", unresolvedExpr)
            : new UnresolvedArgument("whileExpr", new Literal(true, DataType.BOOLEAN)),
        ctx.number != null
            ? new UnresolvedArgument("number", getArgumentValue(ctx.number))
            : new UnresolvedArgument("number", new Literal(10, DataType.INTEGER))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx SortCommandContext instance
   * @return the list of arguments fetched from the sort command
   */
  public static List<Argument> getArgumentList(SortCommandContext ctx) {
    return Arrays.asList(
        ctx.count != null
            ? new Argument("count", getArgumentValue(ctx.count))
            : new Argument("count", new Literal(1000, DataType.INTEGER)),
        ctx.D() != null || ctx.DESC() != null
            ? new Argument("desc", new Literal(true, DataType.BOOLEAN))
            : new Argument("desc", new Literal(false, DataType.BOOLEAN))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx SortFieldContext instance
   * @return the list of arguments fetched from the sort field in sort command
   */
  public static List<Argument> getArgumentList(SortFieldContext ctx) {
    return Arrays.asList(
        ctx.MINUS() != null
            ? new Argument("asc", new Literal(false, DataType.BOOLEAN))
            : new Argument("asc", new Literal(true, DataType.BOOLEAN)),
        ctx.sortFieldExpression().AUTO() != null
            ? new Argument("type", new Literal("auto", DataType.STRING))
            : ctx.sortFieldExpression().IP() != null
            ? new Argument("type", new Literal("ip", DataType.STRING))
            : ctx.sortFieldExpression().NUM() != null
            ? new Argument("type", new Literal("num", DataType.STRING))
            : ctx.sortFieldExpression().STR() != null
            ? new Argument("type", new Literal("str", DataType.STRING))
            : new Argument("type", new Literal(null, DataType.NULL))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx TopCommandContext instance
   * @return the list of arguments fetched from the top command
   */
  public static List<Argument> getArgumentList(TopCommandContext ctx) {
    return Collections.singletonList(
        ctx.number != null
            ? new Argument("noOfResults", getArgumentValue(ctx.number))
            : new Argument("noOfResults", new Literal(10, DataType.INTEGER))
    );
  }

  /**
   * Get list of {@link Argument}.
   *
   * @param ctx RareCommandContext instance
   * @return the list of argument with default number of results for the rare command
   */
  public static List<Argument> getArgumentList(RareCommandContext ctx) {
    return Collections
        .singletonList(new Argument("noOfResults", new Literal(10, DataType.INTEGER)));
  }

  private static Literal getArgumentValue(ParserRuleContext ctx) {
    return ctx instanceof IntegerLiteralContext
        ? new Literal(Integer.parseInt(ctx.getText()), DataType.INTEGER)
        : ctx instanceof BooleanLiteralContext
        ? new Literal(Boolean.valueOf(ctx.getText()), DataType.BOOLEAN)
        : new Literal(StringUtils.unquoteText(ctx.getText()), DataType.STRING);
  }

}
