package org.missinglink.ant.task.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpServerTest extends AbstractHttpTest {

  public HttpServerTest() {
    super();
  }

  @Before
  public void before() throws Exception {
    startHttpServer();
  }

  @After
  public void after() {
    stopHttpServer();
  }

  @Test
  public void pingGet() throws Exception {
    String path = getHttpServerUri() + PING_CONTEXT;
    final URL url = new URL(path);
    final String response = readHttpURL(url);
    Assert.assertEquals(PING_RESPONSE, response);
  }

  @Test
  public void echoGet() throws IOException, InterruptedException {
    final String text = "Hello World";
    String path = getHttpServerUri() + ECHO_CONTEXT + "?" + ECHO_TEXT + "=" + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);
    final String response = readHttpURL(url);
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPost() throws IOException, InterruptedException {
    final String text = "Hello World";
    final HttpURLConnection con = createAndWriteToHttpURLConnection("POST", text);
    final String response = inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPut() throws IOException, InterruptedException {
    final String text = "Hello World";
    final HttpURLConnection con = createAndWriteToHttpURLConnection("PUT", text);
    final String response = inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  private HttpURLConnection createAndWriteToHttpURLConnection(final String method, final String entity) throws MalformedURLException, IOException, ProtocolException {
    String path = getHttpServerUri() + ECHO_CONTEXT;
    final URL url = new URL(path);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod(method);
    con.setDoOutput(true);
    final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(entity);
    out.close();
    return con;
  }
}
