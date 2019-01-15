package org.nlpcn.es4sql.query.planner.resource.blocksize;

/**
 * Block size calculating logic.
 */
public interface BlockSize {

    /**
     * Get block size configured or dynamically. Integer should be sufficient for single block size.
     *
     * @return  block size.
     */
    int size();


    /** Default implementation with fixed block size */
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
