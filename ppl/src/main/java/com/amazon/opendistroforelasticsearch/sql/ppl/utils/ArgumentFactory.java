/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DedupCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.IntegerLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StatsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StringLiteralContext;

public class ArgumentFactory {

    public static List<Expression> getArgumentList(FieldsCommandContext ctx) {
        return new ArrayList<Expression>() {{
            add(ctx.MINUS() != null
                    ? new Argument("exclude", new Literal(true, DataType.BOOLEAN))
                    : new Argument("exclude", new Literal(false, DataType.BOOLEAN)));
        }};
    }

    public static List<Expression> getArgumentList(StatsCommandContext ctx) {
        return new ArrayList<Expression>(){{
            add(ctx.PARTITIONS() != null
                    ? new Argument("partitions", getArgumentValue(ctx.partitions))
                    : new Argument("partitions", new Literal(1, DataType.INTEGER)));
            add(ctx.ALLNUM() != null
                    ? new Argument("allnum", getArgumentValue(ctx.allnum))
                    : new Argument("allnum", new Literal(false, DataType.BOOLEAN)));
            add(ctx.DELIM() != null
                    ? new Argument("delim", getArgumentValue(ctx.delim))
                    : new Argument("delim", new Literal(" ", DataType.STRING)));
            add(ctx.DEDUP_SPLITVALUES() != null
                    ? new Argument("dedupsplit", getArgumentValue(ctx.dedupsplit))
                    : new Argument("dedupsplit", new Literal(false, DataType.BOOLEAN)));
        }};
    }

    public static List<Expression> getArgumentList(DedupCommandContext ctx) {
        return new ArrayList<Expression>() {{
            add(ctx.number != null
                    ? new Argument("number", getArgumentValue(ctx.number))
                    : new Argument("number", new Literal(1, DataType.INTEGER)));

            add(ctx.KEEPEVENTS() != null
                    ? new Argument("keepevents", getArgumentValue(ctx.keeevents))
                    : new Argument("keepevents", new Literal(false, DataType.BOOLEAN)));
            add(ctx.KEEPEMPTY() != null
                    ? new Argument("keepempty", getArgumentValue(ctx.keepempty))
                    : new Argument("keepempty", new Literal(false, DataType.BOOLEAN)));
            add(ctx.CONSECUTIVE() != null
                    ? new Argument("consecutive", getArgumentValue(ctx.consecutive))
                    : new Argument("consecutive", new Literal(false, DataType.BOOLEAN)));
        }};
    }

    public static List<Expression> getArgumentList(SortCommandContext ctx) {
        return new ArrayList<Expression>() {{
            add(ctx.count != null
                    ? new Argument("count", getArgumentValue(ctx.count))
                    : new Argument("count", new Literal(1000, DataType.INTEGER)));
            add(ctx.D() != null || ctx.DESC() != null
                    ? new Argument("desc", new Literal(true, DataType.BOOLEAN))
                    : new Argument("desc", new Literal(false, DataType.BOOLEAN)));
        }};
    }

    private static Expression getArgumentValue(ParserRuleContext ctx) {
        return ctx instanceof IntegerLiteralContext ? new Literal(Integer.parseInt(ctx.getText()), DataType.INTEGER)
                : ctx instanceof DecimalLiteralContext ? new Literal(Double.parseDouble(ctx.getText()), DataType.DOUBLE)
                : ctx instanceof StringLiteralContext ? new Literal(ctx.getText(), DataType.STRING)
                : ctx instanceof BooleanLiteralContext ? new Literal(ctx.getText(), DataType.BOOLEAN)
                : new Literal(null, DataType.NULL);
    }

}
