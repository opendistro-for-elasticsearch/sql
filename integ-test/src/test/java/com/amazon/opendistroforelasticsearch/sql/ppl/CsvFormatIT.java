package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_CSV_SANITIZE;

import java.io.IOException;
import java.util.Locale;
import org.junit.Test;

public class CsvFormatIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK_CSV_SANITIZE);
  }

  @Test
  public void sanitizeTest() throws IOException {
    String result = executeCsvQuery(
        String.format(Locale.ROOT, "source=%s | fields firstname, lastname", TEST_INDEX_BANK_CSV_SANITIZE));
    assertEquals(
        "firstname,lastname\n"
            + "'+Amber JOHnny,Duke Willmington+\n"
            + "'-Hattie,Bond-\n"
            + "'=Nanette,Bates=\n"
            + "'@Dale,Adams@\n"
            + "\",Elinor\",\"Ratliff,,,\"\n",
        result);
  }
}
