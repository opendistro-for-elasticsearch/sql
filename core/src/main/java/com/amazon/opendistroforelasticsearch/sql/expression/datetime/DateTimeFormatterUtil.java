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
  private static final Map<String, String> DATE_FORMAT_MAP =
      ImmutableMap.<String, String>builder()
      .put("%a", "EEE") // %a => EE - Abbreviated weekday name (Sun..Sat)
      .put("%b", "LLL") // %b => LLL - Abbreviated month name (Jan..Dec)
      .put("%c", "MM") // %c => MM - Month, numeric (0..12)
      .put("%d", "dd") // %d => dd - Day of the month, numeric (00..31)
      .put("%e", "d") // %e => d - Day of the month, numeric (0..31)
      .put("%H", "HH") // %H => HH - (00..23)
      .put("%h", "hh") // %h => kk - (01..12)
      .put("%I", "hh") // %I => kk - (01..12)
      .put("%i", "mm") // %i => mm - Minutes, numeric (00..59)
      .put("%j", "DDD") // %j => DDD - (001..366)
      .put("%k", "H") // %k => H - (0..23)
      .put("%l", "h") // %l => h - (1..12)
      .put("%p", "a") // %p => a - AM or PM
      .put("%M", "LLLL") // %M => LLLL - Month name (January..December)
      .put("%m", "MM") // %m => MM - Month, numeric (00..12)
      .put("%r", "hh:mm:ss a") // %r => hh:mm:ss a - hh:mm:ss followed by AM or PM
      .put("%S", "ss") // %S => ss - Seconds (00..59)
      .put("%s", "ss") // %s => ss - Seconds (00..59)
      .put("%T", "HH:mm:ss") // %T => HH:mm:ss -
      .put("%W", "EEEE") // %W => EEEE - Weekday name (Sunday..Saturday)
      .put("%Y", "yyyy") // %Y => yyyy - Year, numeric, 4 digits
      .put("%y", "yy") // %y => yy - Year, numeric, 2 digits
      .build();

  private static final Map<String, Integer> WEEK_FORMAT_MAP = ImmutableMap.<String, Integer>
      builder()
      .put("%U", 0) // %U Week where Sunday is the first day - WEEK() mode 0
      .put("%u", 1) // %u Week where Monday is the first day - WEEK() mode 1
      .put("%V", 2) // %V Week where Sunday is the first day - WEEK() mode 2 used with %X
      .put("%v", 3) // %v Week where Monday is the first day - WEEK() mode 3 used with %x
      .build();
  private static final Map<String, Integer> YEAR_FORMAT_MAP = ImmutableMap.<String, Integer>
      builder()
      .put("%X", 2) // %X Year for week where Sunday is the first day, four digits used with %V
      .put("%x", 3) // %x Year for week where Monday is the first day, four digits used with %v
      .build();

  // %w => Day of week (0=Sunday..6=Saturday)
  private static final String DAY_OF_WEEK = "%w";
  // %f => Microseconds
  private static final String MICROSECONDS = "%f";
  // %D => Day of month with English suffix (0th, 1st ..)
  private static final String DATE_WITH_SUFFIX = "%D";
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
    for (Map.Entry<String, String> dateFormatEntry: DATE_FORMAT_MAP.entrySet()) {
      format = format.replace(dateFormatEntry.getKey(), dateFormatEntry.getValue());
    }

    for (Map.Entry<String, Integer> weekFormatEntry: WEEK_FORMAT_MAP.entrySet()) {
      if (format.contains(weekFormatEntry.getKey())) {
        CalendarLookup calendarLookup = new CalendarLookup(dateExpr);
        format = format.replace(
            weekFormatEntry.getKey(),
            String.format("'%d'", calendarLookup.getWeekNumber(weekFormatEntry.getValue())));
      }
    }
    for (Map.Entry<String, Integer> yearFormatEntry: YEAR_FORMAT_MAP.entrySet()) {
      if (format.contains(yearFormatEntry.getKey())) {
        CalendarLookup calendarLookup = new CalendarLookup(dateExpr);
        format = format.replace(
            yearFormatEntry.getKey(),
            String.format("'%d'", calendarLookup.getYearNumber(yearFormatEntry.getValue())));
      }
    }
    if (format.contains(DATE_WITH_SUFFIX)) {
      int dayOfMonth = date.getDayOfMonth();
      format = format.replace(DATE_WITH_SUFFIX,
          String.format("'%d%s'", dayOfMonth, getSuffix(dayOfMonth)));
    }
    if (format.contains(MICROSECONDS)) {
      format = format.replace(MICROSECONDS,
          String.format("'%d'", (date.getNano() / 1000)));
    }
    if (format.contains(DAY_OF_WEEK)) {
      // Day of week returns 0 indexed, but formatter returns 1 indexed, we need 0.
      format = format.replace(DAY_OF_WEEK,
          String.format("'%d'", date.getDayOfWeek().getValue()));
    }

    format = literalReplace(format);
    // English Locale matches SQL requirements. AM/PM instead of a.m./p.m., Sat instead of Sat. etc
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
    String formattedDate = date.format(formatter);

    // SQL expects PM/AM, but formatter outputs p.m and a.m.
    return new ExprStringValue(formattedDate);
  }

  private static String getSuffix(int val) {
    // Handle special cases.
    if ((SUFFIX_SPECIAL_START_TH <= val) && (val <= SUFFIX_SPECIAL_END_TH)) {
      return SUFFIX_SPECIAL_TH;
    }

    // Check last digit to see if it is special.
    final int lastDigit = val % 10;
    for (Map.Entry<Integer, String> entry: SUFFIX_CONVERTER.entrySet()) {
      if (entry.getKey().equals(lastDigit)) {
        return entry.getValue();
      }
    }

    // Return general case if no specials match.
    return SUFFIX_SPECIAL_TH;
  }

  private static String literalReplace(String format) {
    // Need to do %x=>'x' for any % not listed
    int index = format.indexOf(MOD);
    while (index != -1) {
      if ((index + 2) > format.length()) {
        break;
      }
      String substr = format.substring(index, index + 2);

      format = format.replace(substr, substr.replaceFirst(MOD, QUOTE_LITERAL) + QUOTE_LITERAL);

      // Update index
      index = format.indexOf("%", index);
    }
    return format;
  }
}
