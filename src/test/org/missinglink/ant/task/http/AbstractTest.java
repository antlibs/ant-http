package org.missinglink.ant.task.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractTest {

  protected AbstractTest() {
    super();
  }

  protected String inputStreamToString(final InputStream is) throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    final StringBuilder sb = new StringBuilder();
    String inputLine;

    while ((inputLine = in.readLine()) != null) {
      sb.append(inputLine).append("\n");
    }
    in.close();

    return sb.toString();
  }

}
