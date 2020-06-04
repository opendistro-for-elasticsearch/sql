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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UtilTest {

    @Test
    public void clearEmptyPaths_EmptyMap_ShouldReturnTrue(){
        Map<String,Object> map = new HashMap<>();
        boolean result = Util.clearEmptyPaths(map);
        //
        Assert.assertTrue(result);
    }

    @Test
    public void clearEmptyPaths_EmptyPathSize1_ShouldReturnTrueAndMapShouldBeEmpty(){
        Map<String,Object> map = new HashMap<>();
        map.put("a",new HashMap<String,Object>());
        boolean result = Util.clearEmptyPaths(map);
        Assert.assertTrue(result);
        Assert.assertEquals(0,map.size());
    }

    @Test
    public void clearEmptyPaths_EmptyPathSize2_ShouldReturnTrueAndMapShouldBeEmpty(){
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> innerMap = new HashMap<>();
        innerMap.put("b",new HashMap<String,Object>());
        map.put("a",innerMap);
        boolean result = Util.clearEmptyPaths(map);
        Assert.assertTrue(result);
        Assert.assertEquals(0,map.size());
    }

    @Test
    public void clearEmptyPaths_2PathsOneEmpty_MapShouldBeSizeOne(){
        Map<String,Object> map = new HashMap<>();
        map.put("a",new HashMap<String,Object>());
        map.put("c",1);
        Util.clearEmptyPaths(map);
        Assert.assertEquals(1,map.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void clearEmptyPaths_MapSizeTwoAndTwoOneInnerEmpty_MapShouldBeSizeTwoAndOne(){
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> innerMap = new HashMap<>();
        innerMap.put("b",2);
        innerMap.put("c",new HashMap<String,Object>());
        map.put("a",innerMap);
        map.put("c",1);
        Util.clearEmptyPaths(map);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(1,((HashMap<String,Object>)map.get("a")).size());
    }
}
