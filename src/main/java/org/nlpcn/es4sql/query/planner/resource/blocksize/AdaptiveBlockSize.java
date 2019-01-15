package org.nlpcn.es4sql.query.planner.resource.blocksize;

/**
 * Adaptive block size calculator based on resource usage dynamically.
 */
public class AdaptiveBlockSize implements BlockSize {

    private int upperLimit;

    public AdaptiveBlockSize(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    @Override
    public int size() {
        //TODO: calculate dynamically on each call
        return upperLimit;
    }

    @Override
    public String toString() {
        return "AdaptiveBlockSize with " + "upperLimit=" + upperLimit;
    }
}
