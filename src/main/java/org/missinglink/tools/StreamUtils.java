/*
 *   Copyright Alex Sherwin and other contributors as noted.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.missinglink.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author alex.sherwin
 *
 */
public abstract class StreamUtils {

  protected StreamUtils() {
    super();
  }

  /**
   * Read from an {@link InputStream} and return it as a String.
   *
   * @param is InputStream
   * @return A String created from reading the {@link InputStream}
   * @throws IOException on failure
   */
  public static String inputStreamToString(final InputStream is) throws IOException {

    final BufferedReader in = new BufferedReader(new InputStreamReader(is));

    final StringBuilder sb = new StringBuilder();
    final int pageSize = 1024;
    final byte[] buf = new byte[pageSize];

    int ret = is.read(buf, 0, pageSize);

    while (ret > 0) {
      final byte[] bufPage = new byte[ret];
      System.arraycopy(buf, 0, bufPage, 0, ret);
      sb.append(new String(bufPage));
      ret = is.read(buf, 0, pageSize);
    }
    in.close();

    return sb.toString();
  }

  /**
   * Read from an {@link InputStream} and return it as a <code>byte[]</code>
   * array.
   *
   * @param is InputStream
   * @return A <code>byte[]</code> array created from reading the
   *         {@link InputStream}
   * @throws IOException on failure
   */
  public static byte[] inputStreamToByteArray(final InputStream is) throws IOException {
    return inputStreamToByteArrayOutputStream(is).toByteArray();
  }

  /**
   * Read from an {@link InputStream} and return it as a
   * {@link ByteArrayOutputStream}.
   *
   * @param is InputStream
   * @return A {@link ByteArrayOutputStream} created from reading the
   *         {@link InputStream}
   * @throws IOException on failure
   */
  public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(final InputStream is) throws IOException {

    final BufferedReader in = new BufferedReader(new InputStreamReader(is));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    final int pageSize = 1024;
    final byte[] buf = new byte[pageSize];

    int ret = is.read(buf, 0, pageSize);

    while (ret > 0) {
      final byte[] bufPage = new byte[ret];
      System.arraycopy(buf, 0, bufPage, 0, ret);
      out.write(bufPage);
      ret = is.read(buf, 0, pageSize);
    }
    in.close();

    return out;
  }

}
