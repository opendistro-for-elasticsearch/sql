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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.blocksize;

/**
 * Block size calculating logic.
 */
public interface BlockSize {

    /**
     * Get block size configured or dynamically. Integer should be sufficient for single block size.
     *
     * @return block size.
     */
    int size();


    /**
     * Default implementation with fixed block size
     */
    class FixedBlockSize implements BlockSize {

        private int blockSize;

        public FixedBlockSize(int blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public int size() {
            return blockSize;
        }

        @Override
        public String toString() {
            return "FixedBlockSize with " + "size=" + blockSize;
        }
    }

}
