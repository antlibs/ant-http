package org.missinglink.ant.task.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.missinglink.ant.task.AbstractTest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public abstract class AbstractHttpTest extends AbstractTest {

  protected static final String PING_CONTEXT = "/ping";
  protected static final String PING_RESPONSE = "pong?";

  protected static final String ECHO_CONTEXT = "/echo";
  protected static final String ECHO_TEXT = "text";

  protected int httpServerPort = 10080;
  protected int httpsServerPort = 10443;

  protected HttpServer httpServer;
  protected HttpsServer httpsServer;

  protected AbstractHttpTest() {
    super();
  }

  protected void stopHttpServer() {
    httpServer.stop(0);
  }

  protected void stopHttpsServer() {
    httpsServer.stop(0);
  }

  protected void startHttpServer() throws IOException {
    final InetSocketAddress addr = new InetSocketAddress(httpServerPort);
    httpServer = HttpServer.create(addr, 0);
    httpServer.setExecutor(Executors.newCachedThreadPool());
    attachHttpHandlers(httpServer);
    httpServer.start();
  }

  protected void startHttpsServer() throws Exception {
    final InetSocketAddress addr = new InetSocketAddress(httpsServerPort);
    httpsServer = HttpsServer.create(addr, 0);
    httpsServer.setExecutor(Executors.newCachedThreadPool());
    attachHttpHandlers(httpsServer);

    char[] passphrase = "password".toCharArray();
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream("/keystore.jks"), passphrase);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, passphrase);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    httpsServer.setHttpsConfigurator(new HttpsConfigurator(ssl) {
      public void configure(HttpsParameters params) {

        // get the remote address if needed
        // InetSocketAddress remote = params.getClientAddress();

        SSLContext c = getSSLContext();

        // get the default parameters
        SSLParameters sslparams = c.getDefaultSSLParameters();
        // if (remote.equals (...) ) {
        // modify the default set for client x
        // }

        params.setSSLParameters(sslparams);
        // statement above could throw IAE if any params invalid.
        // eg. if app has a UI and parameters supplied by a user.

      }
    });

    httpsServer.start();
  }

  protected void attachHttpHandlers(final HttpServer server) {

    // ping handler
    server.createContext(PING_CONTEXT, new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, PING_RESPONSE.getBytes().length);
        exchange.getResponseBody().write(PING_RESPONSE.getBytes());
        exchange.getResponseBody().close();
      }
    });

    // echo handler
    server.createContext(ECHO_CONTEXT, new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        String responseEntity = "";
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) || "PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
          responseEntity = inputStreamToString(exchange.getRequestBody());
        } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
          responseEntity = getQueryParams(exchange.getRequestURI()).get(ECHO_TEXT);
        }
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, responseEntity.getBytes().length);
        writeEntity(exchange, responseEntity);
        exchange.close();
      }

      protected void writeEntity(final HttpExchange httpExchange, final String entity) throws IOException {
        httpExchange.getResponseBody().write(entity.getBytes());
      }
    });
  }

  protected Map<String, String> getQueryParams(final URI uri) throws UnsupportedEncodingException {
    final Map<String, String> map = new HashMap<String, String>();
    if (null != uri.getQuery() && uri.getQuery().length() > 0) {
      final String[] params = uri.getQuery().split("&");
      for (final String param : params) {
        final String[] pair = param.split("=");
        map.put(pair[0], pair.length > 1 ? URLDecoder.decode(pair[1], "UTF-8") : null);
      }
    }
    return map;
  }

  protected String getHttpServerUri() {
    return "http://localhost:" + httpServerPort;
  }

  protected String getHttpsServerUri() {
    return "https://localhost:" + httpsServerPort;
  }

  protected String readHttpURL(final URL url) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    return inputStreamToString(conn.getInputStream());
  }

  protected String readHttpsURL(final URL url) throws Exception {
    final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream("/keystore.jks"), "password".toCharArray());
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(null, tmf.getTrustManagers(), null);

    conn.setSSLSocketFactory(ssl.getSocketFactory());

    return inputStreamToString(conn.getInputStream());
  }
}
