/*
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License").
 *  * You may not use this file except in compliance with the License.
 *  * A copy of the License is located at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * or in the "license" file accompanying this file. This file is distributed
 *  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  * express or implied. See the License for the specific language governing
 *  * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.utils.QueryDataMask;
import org.junit.Assert;
import org.junit.Test;

public class QueryDataMaskTest {

    @Test
    public void selectQueriesShouldMaskSensitiveDataWithStars() {
        String maskedQuery = QueryDataMask.maskData("SELECT age FROM accounts");
        String expectedQuery = "SELECT *** FROM ***";
        Assert.assertEquals(maskedQuery, expectedQuery);
    }

    @Test
    public void selectQueriesShouldMaskAllSensitiveData() {
        String query = "SELECT ABS(balance), lastname FROM accounts WHERE age > 30";
        String maskedQuery = QueryDataMask.maskData(query);
        Assert.assertEquals(maskedQuery, "SELECT ABS(***), *** FROM *** WHERE *** > 30");
    }

    @Test
    public void selectQueriesWithQuotedIdentifiersShouldMaskSensitiveData() {
        String maskedQuery = QueryDataMask.maskData("SELECT `age` FROM accounts");
        String expectedQuery = "SELECT *** FROM ***";
        Assert.assertEquals(maskedQuery, expectedQuery);
    }

    @Test
    public void selectQueriesWithQuotedIdentifiersWithSpacesShouldMaskSensitiveData() {
        String maskedQuery = QueryDataMask.maskData("SELECT `a   g e` FROM accounts");
        String expectedQuery = "SELECT *** FROM ***";
        Assert.assertEquals(maskedQuery, expectedQuery);
    }
}
