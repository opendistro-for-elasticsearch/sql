package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.plot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Class to contain plan to render graphs.
 */
@AllArgsConstructor
@Getter
public class PlotPlan {
  private final String fileOutput;
  private final XYSeriesCollection cpuSeriesCollection;
  private final XYSeriesCollection memSeriesCollection;
}
