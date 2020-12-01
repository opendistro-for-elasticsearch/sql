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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public class CsvResponseFormatter implements ResponseFormatter<QueryResult> {
  private static final String INLINE_SEPARATOR = ",";
  private static final String INTERLINE_SEPARATOR = "\n";
  private static final Set<String> SENSITIVE_CHAR = ImmutableSet.of("=", "+", "-", "@");

  @Override
  public String format(QueryResult response) {
    CsvResult result = buildCsvResult(response);
    String headers = String.join(INLINE_SEPARATOR, result.getHeaders());
    ImmutableList.Builder<String> dataLines = new ImmutableList.Builder<>();
    result.getData().forEach(line -> dataLines.add(String.join(INLINE_SEPARATOR, line)));
    return String.join(
        INTERLINE_SEPARATOR, headers, String.join(INTERLINE_SEPARATOR, dataLines.build()));
  }

  @Override
  public String format(Throwable t) {
    return ErrorFormatter.prettyFormat(t);
  }

  private CsvResult buildCsvResult(QueryResult response) {
    CsvResult.Builder builder = CsvResult.builder();
    ImmutableList.Builder<List<String>> dataLines = new ImmutableList.Builder<>();

    response.columnNameTypes().forEach((column, type) -> builder.header(column));
    response.iterator().forEachRemaining(row -> {
      ImmutableList.Builder<String> line = new ImmutableList.Builder<>();
      Arrays.stream(row).forEach(val -> line.add(val.toString()));
      dataLines.add(line.build());
    });
    builder.data(dataLines.build());

    CsvResult result = builder.build();
    return result.sanitize();
  }

  @Builder(builderClassName = "Builder")
  @Getter
  static class CsvResult {
    @Singular("header")
    private List<String> headers;
    private List<List<String>> data;

    /**
     * Migrated from legacy CSV result.
     * To deal with special character in column name and to avoid csv injection.
     * Sanitize both headers and data lines by:
     *  1) First prepend single quote if first char is sensitive (= - + @);
     *  2) Second double quote entire cell if any comma found.
     */
    public CsvResult sanitize() {
      headers = sanitizeHeaders(headers);
      data = sanitizeData(data);
      return this;
    }

    /**
     * Sanitize CSV headers because Elasticsearch allows special character present in field names.
     */
    private List<String> sanitizeHeaders(List<String> headers) {
      return headers.stream()
          .map(this::sanitizeCell)
          .map(cell -> quoteIfRequired(INLINE_SEPARATOR, cell))
          .collect(Collectors.toList());
    }

    /**
     * Sanitize CSV lines in which each cell is sanitized to avoid CSV injection.
     */
    private List<List<String>> sanitizeData(List<List<String>> lines) {
      List<List<String>> result = new ArrayList<>();
      for (List<String> line : lines) {
        result.add(line.stream()
            .map(this::sanitizeCell)
            .map(cell -> quoteIfRequired(INLINE_SEPARATOR, cell))
            .collect(Collectors.toList()));
      }
      return result;
    }

    private String sanitizeCell(String cell) {
      if (isStartWithSensitiveChar(cell)) {
        return "'" + cell;
      }
      return cell;
    }

    private String quoteIfRequired(String separator, String cell) {
      final String quote = "\"";
      return cell.contains(separator)
          ? quote + cell.replaceAll("\"", "\"\"") + quote : cell;
    }

    private boolean isStartWithSensitiveChar(String cell) {
      return SENSITIVE_CHAR.stream().anyMatch(cell::startsWith);
    }
  }

}
