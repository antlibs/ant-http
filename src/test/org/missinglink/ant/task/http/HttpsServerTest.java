package org.missinglink.ant.task.http;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
  public void pingGet() throws Exception {
    String path = getHttpsServerUri() + PING_CONTEXT;
    final URL url = new URL(path);
    final String response = readHttpsURL(url);
    Assert.assertEquals(PING_RESPONSE, response);
  }

  @Test
  public void echoGet() throws Exception {
    final String text = "Hello World";
    String path = getHttpsServerUri() + ECHO_CONTEXT + "?" + ECHO_TEXT + "=" + URLEncoder.encode(text, "UTF-8");
    final URL url = new URL(path);
    final String response = readHttpsURL(url);
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPost() throws Exception {
    final String text = "Hello World";
    final HttpURLConnection con = createAndWriteToHttpURLConnection("POST", text);
    final String response = inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  @Test
  public void echoPut() throws Exception {
    final String text = "Hello World";
    final HttpURLConnection con = createAndWriteToHttpURLConnection("PUT", text);
    final String response = inputStreamToString(con.getInputStream());
    Assert.assertEquals(text, response);
  }

  private HttpURLConnection createAndWriteToHttpURLConnection(final String method, final String entity) throws Exception {
    String path = getHttpsServerUri() + ECHO_CONTEXT;
    final URL url = new URL(path);

    final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream("/keystore.jks"), "password".toCharArray());
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(null, tmf.getTrustManagers(), null);

    con.setSSLSocketFactory(ssl.getSocketFactory());

    con.setRequestMethod(method);
    con.setDoOutput(true);
    final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(entity);
    out.close();
    return con;
  }
}
