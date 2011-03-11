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
    final int pageSize = 1024;
    final byte[] buf = new byte[pageSize];

    int ret = is.read(buf, 0, pageSize);

    while (ret > 0) {
      final byte[] bufPage = new byte[ret];
      for (int i = 0; i < ret; i++) {
        bufPage[i] = buf[i];
      }
      sb.append(new String(bufPage));
      ret = is.read(buf, 0, pageSize);
    }
    in.close();

    return sb.toString();
  }

}
