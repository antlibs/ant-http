package org.missinglink.ant.task.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpsServerTest extends AbstractHttpTest {

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
  public void echoGet() throws IOException, InterruptedException {
    final String text = "Hello World";

    String path = getHttpsServerUri() + ECHO_CONTEXT + "?" + ECHO_TEXT + "="
        + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);

    final String response = readURL(url);

    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPost() throws IOException, InterruptedException {
    final String text = "Hello World";

    String path = getHttpsServerUri() + ECHO_CONTEXT;
    final URL url = new URL(path);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod("POST");
    con.setDoOutput(true);
    final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(text);
    out.close();

    final String response = inputStreamToString(con.getInputStream());

    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPut() throws IOException, InterruptedException {
    final String text = "Hello World";

    String path = getHttpsServerUri() + ECHO_CONTEXT;
    final URL url = new URL(path);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod("PUT");
    con.setDoOutput(true);
    final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(text);
    out.close();

    final String response = inputStreamToString(con.getInputStream());

    Assert.assertEquals(text, response);
  }
}
