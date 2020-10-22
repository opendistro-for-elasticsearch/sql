package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.plot;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResult;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResults;
import com.google.common.collect.ImmutableList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Class to render graphs for output HTML file.
 */
public class PlotRenderer {
  private static final float LINE_WIDTH = 2.0f;
  private static final String DASH = "dash";
  private static final String LINE = "line";
  private static final List<Color> COLOR_LIST = ImmutableList.of(Color.RED, Color.BLUE,
      Color.GREEN, Color.BLACK, Color.MAGENTA, Color.CYAN, Color.YELLOW);
  private static final int WIDTH = 640;
  private static final int HEIGHT = 640;

  /**
   * Private constructor because all functions are static.
   */
  private PlotRenderer() {
  }

  /**
   * Function to render plots using BencharkResults List and Query String List.
   * @param benchmarkResultsList BenchmarkResults List for rendering.
   * @param queryInfoList Query String List for rendering.
   * @throws Exception Thrown if rendering plots fails.
   */
  public static void render(final List<BenchmarkResults> benchmarkResultsList,
                            final List<String> queryInfoList)
      throws Exception {
    final List<PlotPlan> plotPlans = new ArrayList<>();
    final DefaultCategoryDataset barChartDataset = new DefaultCategoryDataset();

    // Get plan for all plots query by query
    for (final String query: queryInfoList) {
      final List<BenchmarkResult> results = new ArrayList<>();
      for (final BenchmarkResults result: benchmarkResultsList) {
        final BenchmarkResult res = result.getByQuery(query);
        results.add(res);
        barChartDataset.addValue(
            res.getExecutionTimeMilliseconds(), res.getType(), query);
      }
      plotPlans.add(getPlan(results, query));
    }
    executePlans(plotPlans, barChartDataset);
  }

  /**
   * Function to execute plans and generate plots.
   * @param plotPlans List of PlotPlan Objects to use for rendering.
   * @throws IOException thrown if writing the file fails.
   */
  private static void executePlans(final List<PlotPlan> plotPlans,
                                   final DefaultCategoryDataset defaultCategoryDataset)
      throws IOException {
    for (final PlotPlan plotPlan : plotPlans) {
      save(plotPlan.getFileOutput(), generateChart(plotPlan)
      );
    }
    save(BenchmarkConstants.SUMMARY_FILE_NAME, generateBarChart(defaultCategoryDataset)
    );
  }

  /**
   * Function to generate bar chart using DefaultCategoryDataset.
   * @param defaultCategoryDataset DefaultCategoryDataset Object to render chart with.
   * @return JFreeChart Object containing bar chart.
   */
  private static JFreeChart generateBarChart(final DefaultCategoryDataset defaultCategoryDataset) {
    return ChartFactory.createBarChart(
        "Query Execution Time",
        "Query",
        "Execution time (ms)",
        defaultCategoryDataset,
        PlotOrientation.HORIZONTAL,
        true, true, false);
  }

  /**
   * Function to get PlotPlan using BenchmarkResult List and query.
   * @param benchmarkResultsList BenchmarkResult List.
   * @param query Query String.
   * @return PlotPlan generated using results.
   */
  private static PlotPlan getPlan(
      final List<BenchmarkResult> benchmarkResultsList, final String query) {
    final XYSeriesCollection cpuSeriesCollection = new XYSeriesCollection();
    final XYSeriesCollection memSeriesCollection = new XYSeriesCollection();

    for (final BenchmarkResult benchmarkResult: benchmarkResultsList) {
      final List<Double> timeSlot = benchmarkResult.getTimeSlots();
      final String type = benchmarkResult.getType();

      // CPU series.
      final List<Double> cpuUsage = benchmarkResult.getCpuUsage();
      final XYSeries cpuSeries = new XYSeries(type + " - Cpu", false, true);
      for (int i = 0; i < cpuUsage.size(); i++) {
        cpuSeries.add(timeSlot.get(i), cpuUsage.get(i));
      }

      // Append additional element to end so that it goes to correct spot. Use interpolation since
      // this datapoint doesn't exist.
      cpuSeries.add((double)benchmarkResult.getExecutionTimeMilliseconds(),
          cpuUsage.get(cpuUsage.size() - 1));

      // Memory series.
      final XYSeries memSeries = new XYSeries(type + " - Mem", false, true);
      final List<Double> memUsage = benchmarkResult.getMemoryUsage();
      for (int i = 0; i < memUsage.size(); i++) {
        memSeries.add(timeSlot.get(i), memUsage.get(i));
      }

      // Append additional element to end so that it goes to correct spot. Use interpolation since
      // this datapoint doesn't exist.
      memSeries.add((double)benchmarkResult.getExecutionTimeMilliseconds(),
          memUsage.get(memUsage.size() - 1));

      cpuSeriesCollection.addSeries(cpuSeries);
      memSeriesCollection.addSeries(memSeries);
    }
    return new PlotPlan(
        BenchmarkConstants.queryToFileName(query), cpuSeriesCollection, memSeriesCollection);
  }

  /**
   * Function to generate chart using PlotPlan.
   * @param plotPlan PlotPlan to generate chart with.
   * @return JFreeChart generated using PlotPlan.
   */
  private static JFreeChart generateChart(final PlotPlan plotPlan) {
    final XYSeriesCollection cpuSeriesCollection = plotPlan.getCpuSeriesCollection();
    final XYSeriesCollection memSeriesCollection = plotPlan.getMemSeriesCollection();

    // Add customization options to chart
    final XYPlot plot = new XYPlot();
    plot.setDataset(0, cpuSeriesCollection);
    plot.setDataset(1, memSeriesCollection);
    plot.setRangeAxis(0, new NumberAxis("CPU Usage (%)"));
    plot.setRangeAxis(1, new NumberAxis("Memory Usage (GB)"));
    //customize the plot with renderers and axis
    final XYItemRenderer cpuRenderer = new XYLineAndShapeRenderer(true, false);
    final XYItemRenderer memRenderer = new XYLineAndShapeRenderer(true, false);
    for (int i = 0; i < cpuSeriesCollection.getSeries().size(); i++) {
      cpuRenderer.setSeriesPaint(i, COLOR_LIST.get(i));
      cpuRenderer.setSeriesStroke(i, getStroke(LINE));
      memRenderer.setSeriesPaint(i, COLOR_LIST.get(i));
      memRenderer.setSeriesPaint(i, COLOR_LIST.get(i));
      memRenderer.setSeriesStroke(i, getStroke(DASH));
    }

    plot.setRenderer(0, cpuRenderer);
    plot.setRenderer(1, memRenderer);
    plot.mapDatasetToRangeAxis(0, 0);
    plot.mapDatasetToRangeAxis(1, 1);
    plot.setDomainAxis(0, new NumberAxis("Time (milliseconds)"));

    ((NumberAxis)plot.getDomainAxis()).setAutoRangeIncludesZero(true);
    ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(true);

    plot.setBackgroundPaint(Color.WHITE);

    // Create chart
    return new JFreeChart(null, null, plot, true);
  }

  /**
   * Function to save chart as a file.
   * @param fileName Name of file.
   * @param chart Chart to save.
   * @throws IOException Thrown if saving the chart fails.
   */
  private static void save(final String fileName, final JFreeChart chart) throws IOException {
    ChartUtilities.saveChartAsJPEG(new File(fileName), chart,
        PlotRenderer.WIDTH, PlotRenderer.HEIGHT);
  }

  /**
   * Function to get Stroke type from line type.
   * @param lineType Line type as String.
   * @return Stroke based on line type.
   */
  private static Stroke getStroke(final String lineType) {
    final float[] dash = { 5.0f };
    final float[] dot = { LINE_WIDTH };
    if (lineType.equals("dash")) {
      return new BasicStroke(
          LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
    } else if (lineType.equals("dot")) {
      return new BasicStroke(
          LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
    } else {
      return new BasicStroke(LINE_WIDTH);
    }
  }
}
