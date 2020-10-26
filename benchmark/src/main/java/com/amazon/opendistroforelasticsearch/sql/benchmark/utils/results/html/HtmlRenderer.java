/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.html;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResult;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResults;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResultsInterpreter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Class to render HTML using BenchmarkResults and QueryInfo.
 */
public class HtmlRenderer {

  /**
   * Private constructor because all functions are static.
   */
  private HtmlRenderer() {
  }

  /**
   * Function to render HTML using BenchmarkResults List and List of query Strings.
   * @param benchmarkResultsList BenchmarkResults List.
   * @param queryInfoList Query String List.
   * @throws Exception Thrown if HTML rendering fails for any reason.
   */
  public static void render(final List<BenchmarkResults> benchmarkResultsList,
                            final List<BenchmarkResultsInterpreter.QueryInfo> queryInfoList)
      throws Exception {
    final ArrayList<HtmlPlan> htmlPlans = new ArrayList<>();
    // Get plan for all plots query by query
    for (final BenchmarkResultsInterpreter.QueryInfo query: queryInfoList) {
      final List<BenchmarkResult> results = new ArrayList<>();
      for (BenchmarkResults result: benchmarkResultsList) {
        results.add(result.getByQuery(query.getQuery()));
      }
      htmlPlans.add(getPlan(results, query.getQueryIdx(), query.getQuery()));
    }
    executePlans(htmlPlans);
  }

  /**
   * Function to get plan to execute using BenchmarkResult List and query.
   * @param benchmarkResultsList BenchmarkResult List.
   * @param query Query.
   * @return HtmlPlan based on BenchmarkResult List and query.
   */
  private static HtmlPlan getPlan(
      final List<BenchmarkResult> benchmarkResultsList, final String queryIdx, final String query) {
    return new HtmlPlan(query, queryIdx, benchmarkResultsList);
  }

  /**
   * Function execute the provided HTML plans.
   * @param plans List of HtmlPlans to use to render the HTML file.
   */
  private static void executePlans(final List<HtmlPlan> plans) throws IOException {
    // Initial setup and css.
    final Document doc = Jsoup.parse("<html></html>");
    doc.body().addClass("body-styles-cls");
    appendCss(doc);

    // Write summary.
    writeSummary(doc, plans);

    // Write details.
    writeDetails(doc, plans);

    // Write query translation table.
    writeQueryTable(doc, plans);

    // Write file.
    final FileWriter htmlFileOutput = new FileWriter(BenchmarkConstants.OUTPUT_HTML_FILE_NAME);
    htmlFileOutput.write(doc.toString());
    htmlFileOutput.close();
  }

  /**
   * Function to write summary.
   * @param doc Document Object to write summary to.
   * @param plans HtmlPlan List to write use to generate summary.
   */
  private static void writeSummary(final Document doc, final List<HtmlPlan> plans) {
    doc.body().appendElement("h2").appendText("Query Execution Time Summary (milliseconds)");
    final Element summaryTable = writeHeadersQuerySummary(doc, plans);
    plans.forEach(p -> writeRowQuerySummary(summaryTable, p.getTableResults(), p.getQuery()));
    doc.body().appendElement("br");
    addImage(doc, BenchmarkConstants.SUMMARY_FILE_NAME);
  }

  /**
   * Function to write details.
   * @param doc Document Object to write details to.
   * @param plans HtmlPlan List to write use to generate details.
   */
  private static void writeDetails(final Document doc, final List<HtmlPlan> plans) {
    doc.body().appendElement("h2").appendText("Query Execution Details");
    plans.forEach(plan -> {
      doc.body().appendElement("h3").appendText(plan.getQueryIdx());
      final Element reportTableHeader = writeHeadersQueryDetails(doc);
      plan.getTableResults().forEach(r -> writeRowQueryDetails(reportTableHeader, r));
      doc.body().appendElement("br");
      addImage(doc, BenchmarkConstants.queryToFileName(plan.getQueryIdx()));
    });
  }

  /**
   * Function to write details.
   * @param doc Document Object to write details to.
   * @param plans HtmlPlan List to write use to generate details.
   */
  private static void writeQueryTable(final Document doc, final List<HtmlPlan> plans) {
    doc.body().appendElement("h2").appendText("Query Index to Query");
    final Element reportTableHeader = writeHeadersQueryInfo(doc);
    plans.forEach(plan -> {
      writeRowQueryInfo(reportTableHeader, plan);
    });
    doc.body().appendElement("br");
  }

  /**
   * Function to append required CSS to beginning of HTML file.
   * @param doc Input document to append to.
   */
  private static void appendCss(final Document doc) {
    doc.head().appendElement("style").appendText("\n"
        + ".center {\n"
        + "  display: block;\n"
        + "  margin-left: auto;\n"
        + "  margin-right: auto;\n"
        + "  width: 25%;\n"
        + "}\n"
        + "table {\n"
        + "  font-family: arial, sans-serif;\n"
        + "  border-collapse: collapse;\n"
        + "  width: 60%;\n"
        + "  margin-left: 10%;\n"
        + "}\n"
        + "\n"
        + "td, th {\n"
        + "  border: 1px solid #6610f2;\n"
        + "  text-align: left;\n"
        + "  padding: 8px;\n"
        + "}\n"
        + "\n"
        + "tr:nth-child(even) {\n"
        + "  background-color: #2797f4;\n"
        + "  color: #ffffff;\n"
        + "}");
  }

  /**
   * Function to write headers for query summary table.
   * @param doc Document to write headers to.
   * @param plans Plan to use to adjust columns.
   * @return Table Element.
   */
  private static Element writeHeadersQuerySummary(final Document doc, final List<HtmlPlan> plans) {
    final Element reportTableHeader = doc.body().appendElement("table");
    reportTableHeader.appendElement("th").appendText("Query");
    plans.get(0).getTableResults().forEach(r ->
        reportTableHeader.appendElement("th").appendText(r.getDatabase()));
    return reportTableHeader;
  }

  /**
   * Function to write a row in the query summary table.
   * @param reportTableHeader Table Element to write row to.
   * @param row TableRow to write.
   * @param query Query that the row belongs to.
   */
  private static void writeRowQuerySummary(
      final Element reportTableHeader, final List<HtmlPlan.TableRow> row, final String query) {
    final Element tr = reportTableHeader.appendElement("tr");
    tr.appendElement("td").appendText(query);
    row.forEach(r -> tr.appendElement("td").appendText(r.getExecutionTime()));
  }

  /**
   * Function to write header of table for query details.
   * @param doc Document to write table to.
   * @return Table Element.
   */
  private static Element writeHeadersQueryDetails(final Document doc) {
    final Element reportTableHeader = doc.body().appendElement("table");
    reportTableHeader.appendElement("th").appendText("Database");
    reportTableHeader.appendElement("th").appendText("Execution Time (ms)");
    final Element memUsage = reportTableHeader.appendElement("th").attr("colspan", "3");
    memUsage.appendText("Memory Usage (GB)");
    memUsage.appendElement("br");
    memUsage.appendText("Min / Average / Max");
    final Element cpuUsage = reportTableHeader.appendElement("th").attr("colspan", "3");
    cpuUsage.appendText("Cpu Usage (%)");
    cpuUsage.appendElement("br");
    cpuUsage.appendText("Min / Average / Max");
    return reportTableHeader;
  }

  /**
   * Function to write query details for a row in the table.
   * @param reportTableHeader Table element.
   * @param row Row plan.
   */
  private static void writeRowQueryDetails(
      final Element reportTableHeader, final HtmlPlan.TableRow row) {
    final Element tr = reportTableHeader.appendElement("tr");
    tr.appendElement("td").appendText(row.getDatabase());
    tr.appendElement("td").appendText(row.getExecutionTime());
    tr.appendElement("td").appendText(row.getMinMem());
    tr.appendElement("td").appendText(row.getAvgMem());
    tr.appendElement("td").appendText(row.getMaxMem());
    tr.appendElement("td").appendText(row.getMinCpu());
    tr.appendElement("td").appendText(row.getAvgCpu());
    tr.appendElement("td").appendText(row.getMaxCpu());
  }

  /**
   * Function to write header of table for query details.
   * @param doc Document to write table to.
   * @return Table Element.
   */
  private static Element writeHeadersQueryInfo(final Document doc) {
    final Element reportTableHeader = doc.body().appendElement("table");
    reportTableHeader.appendElement("th").attr("style", "width: 20%;").appendText("Query Index");
    reportTableHeader.appendElement("th").attr("style", "width: 80%;").appendText("Actual Query");
    return reportTableHeader;
  }

  /**
   * Function to write query details for a row in the table.
   * @param reportTableHeader Table element.
   * @param plan Plan.
   */
  private static void writeRowQueryInfo(
          final Element reportTableHeader, final HtmlPlan plan) {
    final Element tr = reportTableHeader.appendElement("tr");
    tr.appendElement("td").appendText(plan.getQueryIdx());
    tr.appendElement("td").appendText(plan.getQuery());
  }

  /**
   * Function to add image to HTML file.
   * @param doc Input Document.
   * @param imageName Image file.
   */
  private static void addImage(final Document doc, final String imageName) {
    doc.body().appendElement("img").attr("src", imageName)
        .attr("class", "center").attr("width", "30%");
  }
}
