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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints;


import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.yaml.YamlXContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 5/9/2015.
 */
public class HintFactory {

    private static final String PREFIX = "! ";

    public static Hint getHintFromString(String hintAsString) throws SqlParseException {
        if (hintAsString.startsWith("! USE_NESTED_LOOPS") || hintAsString.startsWith("! USE_NL")) {
            return new Hint(HintType.USE_NESTED_LOOPS, null);
        }

        if (hintAsString.startsWith("! SHARD_SIZE")) {
            String[] numbers = getParamsFromHint(hintAsString, "! SHARD_SIZE");
            //todo: check if numbers etc..
            List<Object> params = new ArrayList<>();
            for (String number : numbers) {
                if (number.equals("null") || number.equals("infinity")) {
                    params.add(null);
                } else {
                    params.add(Integer.parseInt(number));
                }
            }
            return new Hint(HintType.SHARD_SIZE, params.toArray());
        }

        if (hintAsString.equals("! HASH_WITH_TERMS_FILTER")) {
            return new Hint(HintType.HASH_WITH_TERMS_FILTER, null);
        }
        if (hintAsString.startsWith("! JOIN_TABLES_LIMIT")) {
            String[] numbers = getParamsFromHint(hintAsString, "! JOIN_TABLES_LIMIT");
            //todo: check if numbers etc..
            List<Object> params = new ArrayList<>();
            for (String number : numbers) {
                if (number.equals("null") || number.equals("infinity")) {
                    params.add(null);
                } else {
                    params.add(Integer.parseInt(number));
                }
            }

            return new Hint(HintType.JOIN_LIMIT, params.toArray());
        }
        if (hintAsString.startsWith("! NL_MULTISEARCH_SIZE")) {
            String[] number = getParamsFromHint(hintAsString, "! NL_MULTISEARCH_SIZE");
            //todo: check if numbers etc..
            int multiSearchSize = Integer.parseInt(number[0]);
            return new Hint(HintType.NL_MULTISEARCH_SIZE, new Object[]{multiSearchSize});
        }
        if (hintAsString.startsWith("! USE_SCROLL")) {
            String[] scrollParams = getParamsFromHint(hintAsString, "! USE_SCROLL");
            if (scrollParams != null && scrollParams.length == 2) {
                String param = scrollParams[0];
                return new Hint(HintType.USE_SCROLL,
                        new Object[]{
                                (param.startsWith("\"") && param.endsWith("\""))
                                        || (param.startsWith("'") && param.endsWith("'"))
                                        ? param.substring(1, param.length() - 1) : Integer.parseInt(param),
                                Integer.parseInt(scrollParams[1])});
            } else {
                return new Hint(HintType.USE_SCROLL, new Object[]{50, 60000});
            }
        }
        if (hintAsString.startsWith("! IGNORE_UNAVAILABLE")) {
            return new Hint(HintType.IGNORE_UNAVAILABLE, null);
        }
        if (hintAsString.startsWith("! DOCS_WITH_AGGREGATION")) {
            Integer[] params = parseParamsAsInts(hintAsString, "! DOCS_WITH_AGGREGATION");
            return new Hint(HintType.DOCS_WITH_AGGREGATION, params);
        }
        if (hintAsString.startsWith("! ROUTINGS")) {
            String[] routings = getParamsFromHint(hintAsString, "! ROUTINGS");
            return new Hint(HintType.ROUTINGS, routings);
        }
        if (hintAsString.startsWith("! HIGHLIGHT")) {
            String[] heighlights = getParamsFromHint(hintAsString, "! HIGHLIGHT");
            ArrayList hintParams = new ArrayList();
            hintParams.add(heighlights[0]);
            if (heighlights.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < heighlights.length; i++) {
                    if (i != 1) {
                        builder.append("\n");
                    }
                    builder.append(heighlights[i]);
                }
                String heighlightParam = builder.toString();
                YAMLFactory yamlFactory = new YAMLFactory();
                YAMLParser yamlParser = null;
                try {
                    yamlParser = yamlFactory.createParser(heighlightParam.toCharArray());
                    YamlXContentParser yamlXContentParser = new YamlXContentParser(NamedXContentRegistry.EMPTY,
                            LoggingDeprecationHandler.INSTANCE, yamlParser);
                    Map<String, Object> map = yamlXContentParser.map();
                    hintParams.add(map);
                } catch (IOException e) {
                    throw new SqlParseException("could not parse heighlight hint: " + e.getMessage());
                }
            }
            return new Hint(HintType.HIGHLIGHT, hintParams.toArray());
        }
        if (hintAsString.startsWith("! MINUS_SCROLL_FETCH_AND_RESULT_LIMITS")) {
            Integer[] params = parseParamsAsInts(hintAsString, "! MINUS_SCROLL_FETCH_AND_RESULT_LIMITS");
            if (params.length > 3) {
                throw new SqlParseException("MINUS_FETCH_AND_RESULT_LIMITS should have 3 int params "
                        + "(maxFromFirst,maxFromSecond,hitsPerScrollShard)");
            }
            Integer[] paramsWithDefaults = new Integer[3];
            int defaultMaxFetchFromTable = 100000;
            int defaultFetchOnScroll = 1000;
            paramsWithDefaults[0] = defaultMaxFetchFromTable;
            paramsWithDefaults[1] = defaultMaxFetchFromTable;
            paramsWithDefaults[2] = defaultFetchOnScroll;
            for (int i = 0; i < params.length; i++) {
                paramsWithDefaults[i] = params[i];
            }

            return new Hint(HintType.MINUS_FETCH_AND_RESULT_LIMITS, paramsWithDefaults);
        }
        if (hintAsString.startsWith("! MINUS_USE_TERMS_OPTIMIZATION")) {
            String[] param = getParamsFromHint(hintAsString, "! MINUS_USE_TERMS_OPTIMIZATION");
            boolean shouldLowerStringOnTerms = false;
            if (param != null) {
                if (param.length != 1) {
                    throw new SqlParseException(
                            "MINUS_USE_TERMS_OPTIMIZATION should have none or one boolean param: false/true ");
                }
                try {
                    shouldLowerStringOnTerms = Boolean.parseBoolean(param[0].toLowerCase());
                } catch (Exception e) {
                    throw new SqlParseException("MINUS_USE_TERMS_OPTIMIZATION should have none or one boolean param: "
                            + "false/true , got:" + param[0]);
                }
            }
            return new Hint(HintType.MINUS_USE_TERMS_OPTIMIZATION, new Object[]{shouldLowerStringOnTerms});
        }
        if (hintAsString.startsWith("! COLLAPSE")) {
            String collapse = getParamFromHint(hintAsString, "! COLLAPSE");
            return new Hint(HintType.COLLAPSE, new String[]{collapse});
        }
        if (hintAsString.startsWith("! POST_FILTER")) {
            String postFilter = getParamFromHint(hintAsString, "! POST_FILTER");
            return new Hint(HintType.POST_FILTER, new String[]{postFilter});
        }

        Hint queryPlanHint = parseHintForQueryPlanner(hintAsString);
        if (queryPlanHint != null) {
            return queryPlanHint;
        }

        return null;
    }

    /**
     * Parse hints for hash join in new query planning framework.
     * Only check syntax error here and leave semantics interpret work for planner.
     */
    private static Hint parseHintForQueryPlanner(String hintStr) {
        if (hintStr.contains("(")
                && (hintStr.startsWith("! JOIN_ALGORITHM_BLOCK_SIZE")
                        || hintStr.startsWith("! JOIN_SCROLL_PAGE_SIZE")
                        || hintStr.startsWith("! JOIN_CIRCUIT_BREAK_LIMIT")
                        || hintStr.startsWith("! JOIN_BACK_OFF_RETRY_INTERVALS")
                        || hintStr.startsWith("! JOIN_TIME_OUT")
                )) { // Note that Trie tree is needed here if many hint options

            String hintName = hintStr.substring(PREFIX.length(), hintStr.indexOf('(')).trim();
            String hintPrefix = PREFIX + hintName;
            HintType hintType = HintType.valueOf(hintName);
            Integer[] params = parseParamsAsInts(hintStr, hintPrefix);

            if (params != null && params.length > 0) {
                return new Hint(hintType, params);
            }
        } else if (hintStr.startsWith("! JOIN_ALGORITHM_USE_LEGACY")) {
            return new Hint(HintType.JOIN_ALGORITHM_USE_LEGACY, new Object[0]);
        }
        return null;
    }

    private static String getParamFromHint(String hint, String prefix) {
        if (!hint.contains("(")) {
            return null;
        }
        return hint.replace(prefix, "").replaceAll("\\s*\\(\\s*", "").replaceAll("\\s*\\,\\s*", ",")
                .replaceAll("\\s*\\)\\s*", "");
    }

    private static String[] getParamsFromHint(String hint, String prefix) {
        String param = getParamFromHint(hint, prefix);
        return param != null ? param.split(",") : null;
    }

    private static Integer[] parseParamsAsInts(String hintAsString, String startWith) {
        String[] number = getParamsFromHint(hintAsString, startWith);
        if (number == null) {
            return new Integer[0];
        }
        //todo: check if numbers etc..
        Integer[] params = new Integer[number.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = Integer.parseInt(number[i]);
        }
        return params;
    }


}
