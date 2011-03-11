package org.missinglink.ant.task.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public abstract class AbstractHttpTest extends AbstractTest {

  protected static final String PING_CONTEXT = "/ping";
  protected static final String PING_RESPONSE = "pong?";

  protected static final String ECHO_CONTEXT = "/echo";
  protected static final String ECHO_TEXT = "text";

  protected int httpServerPort = 10080;
  protected HttpServer httpServer;

  protected AbstractHttpTest() {
    super();
  }

  protected void startHttpServer() throws IOException {

    final InetSocketAddress addr = new InetSocketAddress(httpServerPort);
    httpServer = HttpServer.create(addr, 0);
    httpServer.setExecutor(Executors.newCachedThreadPool());

    // ping handler
    httpServer.createContext(PING_CONTEXT, new HttpHandler() {
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
    httpServer.createContext(ECHO_CONTEXT, new HttpHandler() {
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

    httpServer.start();
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
