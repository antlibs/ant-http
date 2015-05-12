/**
 *   Copyright 2011 Alex Sherwin
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

package org.missinglink.ant.task.http.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.missinglink.tools.StreamUtils;

/**
 *
 * @author alex.sherwin
 *
 */
public class HttpsServerTest extends AbstractHttpServerTest {

  public HttpsServerTest() {
    super();
  }

  @Before
  public void before() throws Exception {
    startHttpsServer();
  }

  @After
  public void after() {
    stopHttpsServer();
  }

  @Test
  public void pingGet() throws Exception {
    final String path = getHttpsServerUri() + PING_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    final String response = StreamUtils.inputStreamToString(conn.getInputStream());
    Assert.assertEquals(PING_RESPONSE, response);
  }

  @Test
  public void pingSecureGetAuthFailure() throws Exception {
    final String path = getHttpsServerUri() + SECURE_CONTEXT + PING_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    try {
      StreamUtils.inputStreamToString(conn.getInputStream());
      Assert.assertTrue("Authentication should have failed", false);
    } catch (final IOException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, conn.getResponseCode());
    }
  }

  @Test
  public void pingSecureGet() throws Exception {
    final String path = getHttpsServerUri() + SECURE_CONTEXT + PING_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    addAuthenticationHeader(conn);
    final String response = StreamUtils.inputStreamToString(conn.getInputStream());
    Assert.assertEquals(PING_RESPONSE, response);

  }

  @Test
  public void echoGet() throws Exception {
    final String text = "Hello World";
    final String path = getHttpsServerUri() + ECHO_CONTEXT + "?" + ECHO_TEXT + "=" + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    final String response = StreamUtils.inputStreamToString(conn.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoGetSecureAuthFailure() throws Exception {
    final String text = "Hello World";
    final String path = getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT + "?" + ECHO_TEXT + "=" + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    try {
      StreamUtils.inputStreamToString(conn.getInputStream());
      Assert.assertTrue("Authentication should have failed", false);
    } catch (final IOException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, conn.getResponseCode());
    }
  }

  @Test
  public void echoGetSecure() throws Exception {
    final String text = "Hello World";
    final String path = getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT + "?" + ECHO_TEXT + "=" + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    addAuthenticationHeader(conn);
    final String response = StreamUtils.inputStreamToString(conn.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPost() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("POST", getHttpsServerUri() + ECHO_CONTEXT, text, false);
    final String response = StreamUtils.inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPostSecureAuthFailure() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("POST", getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT, text, false);
    try {
      StreamUtils.inputStreamToString(con.getInputStream());
      Assert.assertTrue("Authentication should have failed", false);
    } catch (final IOException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
    }
  }

  @Test
  public void echoPostSecure() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("POST", getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT, text, true);
    final String response = StreamUtils.inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPut() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("PUT", getHttpsServerUri() + ECHO_CONTEXT, text, false);
    final String response = StreamUtils.inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPutSecureAuthFailure() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("PUT", getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT, text, false);
    try {
      StreamUtils.inputStreamToString(con.getInputStream());
      Assert.assertTrue("Authentication should have failed", false);
    } catch (final IOException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, con.getResponseCode());
    }
  }

  @Test
  public void echoPutSecure() throws Exception {
    final String text = "Hello World";
    final HttpsURLConnection con = createAndWriteToHttpsURLConnection("PUT", getHttpsServerUri() + SECURE_CONTEXT + ECHO_CONTEXT, text, true);
    final String response = StreamUtils.inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void helloWorldZipGet() throws Exception {
    final String path = getHttpsServerUri() + HW_ZIP_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    final byte[] response = StreamUtils.inputStreamToByteArray(conn.getInputStream());
    final byte[] hwZip = StreamUtils.inputStreamToByteArray(getClass().getResourceAsStream(HW_ZIP));
    Assert.assertArrayEquals(hwZip, response);
  }

  @Test
  public void helloWorldZipGetAuthFailure() throws Exception {
    final String path = getHttpsServerUri() + SECURE_CONTEXT + HW_ZIP_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    try {
      StreamUtils.inputStreamToByteArray(conn.getInputStream());
      Assert.assertTrue("Authentication should have failed", false);
    } catch (final IOException e) {
      Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, conn.getResponseCode());
    }
  }

  @Test
  public void helloWorldZipSecureGet() throws Exception {
    final String path = getHttpsServerUri() + SECURE_CONTEXT + HW_ZIP_CONTEXT;
    final URL url = new URL(path);
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    attachSSLSocketFactory(conn);
    addAuthenticationHeader(conn);
    final byte[] response = StreamUtils.inputStreamToByteArray(conn.getInputStream());
    final byte[] hwZip = StreamUtils.inputStreamToByteArray(getClass().getResourceAsStream(HW_ZIP));
    Assert.assertArrayEquals(hwZip, response);
  }
}
