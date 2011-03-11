package org.missinglink.ant.task.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

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

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, passphrase);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);

    SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    httpsServer.setHttpsConfigurator(new HttpsConfigurator(ssl) {
      public void configure(HttpsParameters params) {

        System.out.println("Here..");
        
        // get the remote address if needed
        InetSocketAddress remote = params.getClientAddress();

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
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write(PING_RESPONSE.getBytes());
        exchange.getResponseBody().close();
        exchange.close();
      }
    });

    // echo handler
    server.createContext(ECHO_CONTEXT, new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())
            || "PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
          writeEntity(exchange, inputStreamToString(exchange.getRequestBody()));
        } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
          writeEntity(exchange,
              getQueryParams(exchange.getRequestURI()).get(ECHO_TEXT));
        }
        exchange.close();
      }

      protected void writeEntity(final HttpExchange httpExchange,
          final String entity) throws IOException {
        httpExchange.getResponseBody().write(entity.getBytes());
        httpExchange.getResponseBody().close();
      }
    });
  }

  protected Map<String, String> getQueryParams(final URI uri) {
    final Map<String, String> map = new HashMap<String, String>();
    if (null != uri.getQuery() && uri.getQuery().length() > 0) {
      final String[] params = uri.getQuery().split("&");
      for (final String param : params) {
        final String[] pair = param.split("=");
        map.put(pair[0], pair.length > 1 ? pair[1] : null);
      }
    }
    return map;
  }
}
