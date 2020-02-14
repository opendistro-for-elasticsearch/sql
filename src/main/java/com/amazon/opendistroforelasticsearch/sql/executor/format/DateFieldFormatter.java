package com.amazon.opendistroforelasticsearch.sql.executor.format;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DateFieldFormatter
{
  private static final Logger LOG = LogManager.getLogger(DateFieldFormatter.class);
  public static final String JDBC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  private final Map<String, String> dateFieldFormatMap;
  private List<Schema.Column> columns;

  public DateFieldFormatter(Map<String, String> dateFieldFormatMap, List<Schema.Column> columns)
  {
    this.dateFieldFormatMap = dateFieldFormatMap;
    this.columns = columns;
  }

  public void applyJDBCDateFormat(Map<String, Object> rowSource) {
    for (Schema.Column column : columns) {
      String columnType = column.getType();
      String columnName = column.getName();

      LOG.info("{} ({})", columnName, columnType);
      if (columnType.equals(Schema.Type.DATE.nameLowerCase())) {
        String columnFormat = dateFieldFormatMap.get(columnName);
        LOG.info("(CFFF) {} : {}", columnName, columnFormat);
        DateFormat format = DateFormat.valueOf(columnFormat.toUpperCase());

        LOG.info("(FORMATTING) {} using {}", rowSource.get(columnName), format.getFormatString());
        Object columnOriginalDate = rowSource.get(columnName);
        if (columnOriginalDate == null) {
          continue;
        }

        Date date = parseDateString(format, columnOriginalDate.toString());

        if (date != null) {
          LOG.info("(FORMATTING) {} using {}", columnOriginalDate.toString(), format.getFormatString());
          String clientFormattedDate = format.getFormattedDate(date, JDBC_DATE_FORMAT);
          // String dataString = String.format("[YES] %s -> %s", columnOriginalDate.toString(), clientFormattedDate);
          String dataString = String.format("%s", clientFormattedDate);
          rowSource.put(columnName, dataString);
        } else {
          LOG.warn("Could not parse date value; returning original value");
        }
      }
    }
  }

  private Date parseDateString(DateFormat format, String columnOriginalDate)
  {
    try {
      switch (format) {
        case DATE_OPTIONAL_TIME:
          return DateUtils.parseDate(
              columnOriginalDate,
              "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
              "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              "yyyy-MM-dd'T'HH:mm:ss",
              "yyyy-MM-dd'T'HH:mm:ssXXX",
              "yyyy-MM-dd");
        case EPOCH_MILLIS:
          return new Date(Long.parseLong(columnOriginalDate));
        case EPOCH_SECOND:
          return new Date(Long.parseLong(columnOriginalDate) * 1000);
        default:
          return DateUtils.parseDate(columnOriginalDate, format.getFormatString());
      }
    }
    catch (ParseException e) {
      LOG.error(String.format("Error parsing date string %s as %s", columnOriginalDate, format.nameLowerCase()), e);
    }
    return null;
  }

}
