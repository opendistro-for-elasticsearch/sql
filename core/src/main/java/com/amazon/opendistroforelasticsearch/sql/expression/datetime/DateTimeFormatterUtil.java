package com.amazon.opendistroforelasticsearch.sql.expression.datetime;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * This class converts a SQL style DATE_FORMAT format specifier and converts it to a
 * Java SimpleDateTime format.
 */
class DateTimeFormatterUtil {
  private static final int SUFFIX_SPECIAL_START_TH = 11;
  private static final int SUFFIX_SPECIAL_END_TH = 13;
  private static final String SUFFIX_SPECIAL_TH = "th";
  private static final Map<Integer, String> SUFFIX_CONVERTER =
      ImmutableMap.<Integer, String>builder()
      .put(1, "st").put(2, "nd").put(3, "rd").build();

  // The following have special cases that need handling outside of the format options provided
  // by the DateTimeFormatter class.
  interface DateTimeFormatHandler {
    String getFormat(LocalDateTime date);
  }

  private static final Map<String, DateTimeFormatHandler> HANDLERS =
      ImmutableMap.<String, DateTimeFormatHandler>builder()
      .put("%a", (date) -> "EEE") // %a => EEE - Abbreviated weekday name (Sun..Sat)
      .put("%b", (date) -> "LLL") // %b => LLL - Abbreviated month name (Jan..Dec)
      .put("%c", (date) -> "MM") // %c => MM - Month, numeric (0..12)
      .put("%d", (date) -> "dd") // %d => dd - Day of the month, numeric (00..31)
      .put("%e", (date) -> "d") // %e => d - Day of the month, numeric (0..31)
      .put("%H", (date) -> "HH") // %H => HH - (00..23)
      .put("%h", (date) -> "hh") // %h => hh - (01..12)
      .put("%I", (date) -> "hh") // %I => hh - (01..12)
      .put("%i", (date) -> "mm") // %i => mm - Minutes, numeric (00..59)
      .put("%j", (date) -> "DDD") // %j => DDD - (001..366)
      .put("%k", (date) -> "H") // %k => H - (0..23)
      .put("%l", (date) -> "h") // %l => h - (1..12)
      .put("%p", (date) -> "a") // %p => a - AM or PM
      .put("%M", (date) -> "LLLL") // %M => LLLL - Month name (January..December)
      .put("%m", (date) -> "MM") // %m => MM - Month, numeric (00..12)
      .put("%r", (date) -> "hh:mm:ss a") // %r => hh:mm:ss a - hh:mm:ss followed by AM or PM
      .put("%S", (date) -> "ss") // %S => ss - Seconds (00..59)
      .put("%s", (date) -> "ss") // %s => ss - Seconds (00..59)
      .put("%T", (date) -> "HH:mm:ss") // %T => HH:mm:ss
      .put("%W", (date) -> "EEEE") // %W => EEEE - Weekday name (Sunday..Saturday)
      .put("%Y", (date) -> "yyyy") // %Y => yyyy - Year, numeric, 4 digits
      .put("%y", (date) -> "yy") // %y => yy - Year, numeric, 2 digits
      // The following are not directly supported by DateTimeFormatter.
      .put("%D", (date) -> // %w - Day of month with English suffix
          String.format("'%d%s'", date.getDayOfMonth(), getSuffix(date.getDayOfMonth())))
      .put("%f", (date) -> // %f - Microseconds
          String.format("'%d'", (date.getNano() / 1000)))
      .put("%w", (date) -> // %w - Day of week (0 indexed)
          String.format("'%d'", date.getDayOfWeek().getValue()))
      .put("%U", (date) -> // %U Week where Sunday is the first day - WEEK() mode 0
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getWeekNumber(0)))
      .put("%u", (date) -> // %u Week where Monday is the first day - WEEK() mode 1
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getWeekNumber(1)))
      .put("%V", (date) -> // %V Week where Sunday is the first day - WEEK() mode 2 used with %X
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getWeekNumber(2)))
      .put("%v", (date) -> // %v Week where Monday is the first day - WEEK() mode 3 used with %x
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getWeekNumber(3)))
      .put("%X", (date) -> // %X Year for week where Sunday is the first day, 4 digits used with %V
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getYearNumber(2)))
      .put("%x", (date) -> // %x Year for week where Monday is the first day, 4 digits used with %v
          String.format("'%d'", new CalendarLookup(date.toLocalDate()).getYearNumber(3)))
      .build();

  private static final String MOD = "%";
  private static final String QUOTE_LITERAL = "'";

  private DateTimeFormatterUtil() {
  }

  /**
   * Format the date using the date format String.
   * @param dateExpr the date ExprValue of Date/Datetime/Timestamp/String type.
   * @param formatExpr the format ExprValue of String type.
   * @return Date formatted using format and returned as a String.
   */
  static ExprValue getFormattedDate(ExprValue dateExpr, ExprValue formatExpr) {
    String format = formatExpr.stringValue();
    final LocalDateTime date = dateExpr.datetimeValue();
    for (Map.Entry<String, DateTimeFormatHandler> handler: HANDLERS.entrySet()) {
      if (format.contains(handler.getKey())) {
        format = format.replace(handler.getKey(), handler.getValue().getFormat(date));
      }
    }
    format = literalReplace(format);

    // English Locale matches SQL requirements.
    // 'AM'/'PM' instead of 'a.m.'/'p.m.'
    // 'Sat' instead of 'Sat.' etc
    return new ExprStringValue(date.format(DateTimeFormatter.ofPattern(format, Locale.ENGLISH)));
  }

  /**
   * Returns English suffix of incoming value.
   * @param val Incoming value.
   * @return English suffix as String (st, nd, rd, th)
   */
  private static String getSuffix(int val) {
    // The numbers 11, 12, and 13 do not follow general suffix rules.
    if ((SUFFIX_SPECIAL_START_TH <= val) && (val <= SUFFIX_SPECIAL_END_TH)) {
      return SUFFIX_SPECIAL_TH;
    }
    return SUFFIX_CONVERTER.getOrDefault(val % 10, SUFFIX_SPECIAL_TH);
  }

  /**
   * Goes through format String and replaces any %x with 'x' where x is unmapped.
   * @param format Incoming format String without mapping completed.
   * @return Outgoing format String with mapping completed.
   */
  private static String literalReplace(String format) {
    // Need to do %x=>'x' for any % not listed
    int index = format.indexOf(MOD);
    while ((index != -1) && ((index + 2) <= format.length())) {
      String substr = format.substring(index, index + 2);
      format = format.replace(substr, substr.replaceFirst(MOD, QUOTE_LITERAL) + QUOTE_LITERAL);
      index = format.indexOf(MOD, index + 2);
    }
    return format;
  }
}
