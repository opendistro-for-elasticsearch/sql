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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents abstract query. every query
 * has indexes, types, and where clause.
 */
public abstract class Query implements QueryStatement {

    private Where where = null;
    private List<From> from = new ArrayList<>();


    public Where getWhere() {
        return this.where;
    }

    public void setWhere(Where where) {
        this.where = where;
    }

    public List<From> getFrom() {
        return from;
    }


    /**
     * Get the indexes the query refer to.
     *
     * @return list of strings, the indexes names
     */
    public String[] getIndexArr() {
        String[] indexArr = new String[this.from.size()];
        for (int i = 0; i < indexArr.length; i++) {
            indexArr[i] = this.from.get(i).getIndex();
        }
        return indexArr;
    }

    /**
     * Get the types the query refer to.
     *
     * @return list of strings, the types names
     */
    public String[] getTypeArr() {
        List<String> list = new ArrayList<>();
        From index = null;
        for (int i = 0; i < from.size(); i++) {
            index = from.get(i);
            if (index.getType() != null && index.getType().trim().length() > 0) {
                list.add(index.getType());
            }
        }
        if (list.size() == 0) {
            return null;
        }

        return list.toArray(new String[list.size()]);
    }
}
