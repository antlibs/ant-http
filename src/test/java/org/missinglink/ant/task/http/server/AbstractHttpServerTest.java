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

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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

import org.missinglink.ant.task.http.AbstractTest;
import org.missinglink.http.encoding.Base64;
import org.missinglink.tools.StreamUtils;

/**
 *
 * @author alex.sherwin
 *
 */
public abstract class AbstractHttpServerTest extends AbstractTest {

  protected static final String PING_CONTEXT = "/ping";
  protected static final String PING_RESPONSE = "pong?";

  protected static final String ECHO_CONTEXT = "/echo";
  protected static final String ECHO_TEXT = "text";

  protected static final String MOVED_PERM_CONTEXT = "/301";
  protected static final String MOVED_PERM_RESPONSE = "Moved Permanently";

  protected static final String MOVED_TEMP_CONTEXT = "/302";
  protected static final String MOVED_TEMP_RESPONSE = "Found";

  protected static final String SEE_OTHER_CONTEXT = "/303";
  protected static final String SEE_OTHER_RESPONSE = "See Other";

  protected static final String INTERNAL_SERVER_ERROR_CONTEXT = "/500";
  protected static final String INTERNAL_SERVER_ERROR_RESPONSE = "Internal Server Error";

  protected static final String SECURE_CONTEXT = "/secure";

  protected static final String KEYSTORE = "/keystore.jks";

  protected static final String USERNAME = "user";
  protected static final String PASSWORD = "password";

  protected static final String HW_ZIP_CONTEXT = "/hwzip";
  protected static final String HW_PNG_CONTEXT = "/hwpng";

  protected static final String HW_ZIP = "/hw.zip";
  protected static final String HW_PNG = "/hw.png";

  protected final int httpServerPort = 10080;
  protected final int httpsServerPort = 10443;

  protected HttpServer httpServer;
  protected HttpsServer httpsServer;

  protected AbstractHttpServerTest() {
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

    final char[] passphrase = "password".toCharArray();
    final KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream(KEYSTORE), passphrase);

    final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, passphrase);

    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    final SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    httpsServer.setHttpsConfigurator(new HttpsConfigurator(ssl) {
      @Override
      public void configure(final HttpsParameters params) {
        final SSLContext c = getSSLContext();
        final SSLParameters sslparams = c.getDefaultSSLParameters();
        params.setSSLParameters(sslparams);
      }
    });

    httpsServer.start();
  }

  protected void attachHttpHandlers(final HttpServer server) {
    // ping handler
    server.createContext(PING_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        pingResponse(exchange);
      }
    });

    // secure ping handler
    final HttpContext securePingContext = server.createContext(SECURE_CONTEXT + PING_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        pingResponse(exchange);
      }
    });
    securePingContext.setAuthenticator(getBasicAuthenticator());

    // echo handler
    server.createContext(ECHO_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        echoResponse(exchange);
      }
    });

    // secure echo handler
    final HttpContext secureEchoContext = server.createContext(SECURE_CONTEXT + ECHO_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        echoResponse(exchange);
      }
    });
    secureEchoContext.setAuthenticator(getBasicAuthenticator());

    // 301 handler
    server.createContext(MOVED_PERM_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", PING_CONTEXT);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(301, MOVED_PERM_RESPONSE.getBytes().length);
        exchange.getResponseBody().write(MOVED_PERM_RESPONSE.getBytes());
        exchange.getResponseBody().close();
      }
    });

    // 302 handler
    server.createContext(MOVED_TEMP_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", PING_CONTEXT);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(302, MOVED_TEMP_RESPONSE.getBytes().length);
        exchange.getResponseBody().write(MOVED_TEMP_RESPONSE.getBytes());
        exchange.getResponseBody().close();
      }
    });

    // 303 handler
    server.createContext(SEE_OTHER_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", PING_CONTEXT);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(303, SEE_OTHER_RESPONSE.getBytes().length);
        exchange.getResponseBody().write(SEE_OTHER_RESPONSE.getBytes());
        exchange.getResponseBody().close();
      }
    });

    // 500 handler
    server.createContext(INTERNAL_SERVER_ERROR_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        internalErrorResponse(exchange);
      }
    });

    // secure 500 handler
    final HttpContext secure500Context = server.createContext(SECURE_CONTEXT + INTERNAL_SERVER_ERROR_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        internalErrorResponse(exchange);
      }
    });
    secure500Context.setAuthenticator(getBasicAuthenticator());

    // hw zip handler
    server.createContext(HW_ZIP_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        zipResponse(exchange);
      }
    });

    // secure hw zip handler
    final HttpContext hwZipContext = server.createContext(SECURE_CONTEXT + HW_ZIP_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
		zipResponse(exchange);
      }
    });
    hwZipContext.setAuthenticator(getBasicAuthenticator());

    // hw png handler
    server.createContext(HW_PNG_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
		imageResponse(exchange);
      }
    });

    // secure hw png handler
    final HttpContext hwPngContext = server.createContext(SECURE_CONTEXT + HW_PNG_CONTEXT, new HttpHandler() {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
		imageResponse(exchange);
      }
    });
    hwPngContext.setAuthenticator(getBasicAuthenticator());
  }

  private void echoResponse(HttpExchange exchange) throws IOException {
    String responseEntity = "";
    if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) || "PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
      responseEntity = StreamUtils.inputStreamToString(exchange.getRequestBody());
    } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
      responseEntity = getQueryParams(exchange.getRequestURI()).get(ECHO_TEXT);
    }
    exchange.getResponseHeaders().set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(200, responseEntity.getBytes().length);
    writeEntity(exchange, responseEntity);
    exchange.close();
  }

  private void writeEntity(final HttpExchange httpExchange, final String entity) throws IOException {
    httpExchange.getResponseBody().write(entity.getBytes());
  }

  private void internalErrorResponse(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(500, INTERNAL_SERVER_ERROR_RESPONSE.getBytes().length);
    exchange.getResponseBody().write(INTERNAL_SERVER_ERROR_RESPONSE.getBytes());
    exchange.getResponseBody().close();
  }

  private void zipResponse(HttpExchange exchange) throws IOException {
    final InputStream is = getClass().getResourceAsStream(HW_ZIP);
    final byte[] bytes = StreamUtils.inputStreamToByteArray(is);
    exchange.getResponseHeaders().set("Content-Type", "application/zip");
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
  }

  private void imageResponse(HttpExchange exchange) throws IOException {
	final InputStream is = getClass().getResourceAsStream(HW_PNG);
	final byte[] bytes = StreamUtils.inputStreamToByteArray(is);
	exchange.getResponseHeaders().set("Content-Type", "image/png");
	exchange.sendResponseHeaders(200, bytes.length);
	exchange.getResponseBody().write(bytes);
	exchange.getResponseBody().close();
  }

  private void pingResponse(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(200, PING_RESPONSE.getBytes().length);
    exchange.getResponseBody().write(PING_RESPONSE.getBytes());
    exchange.getResponseBody().close();
  }

  protected Map<String, String> getQueryParams(final URI uri) throws UnsupportedEncodingException {
    final Map<String, String> map = new HashMap<String, String>();
    if (null != uri.getQuery() && uri.getQuery().length() > 0) {
      final String[] params = uri.getQuery().split("&");
      for (final String param : params) {
        final String[] pair = param.split("=", 2);
        map.put(pair[0], (pair.length > 1 && !pair[1].equals("")) ? URLDecoder.decode(pair[1], "UTF-8") : null);
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

  // protected String readHttpURL(final URL url) throws IOException {
  // final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
  // return StreamUtils.inputStreamToString(conn.getInputStream());
  // }

  protected void attachSSLSocketFactory(final HttpsURLConnection conn) throws Exception {

    final KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream(KEYSTORE), "password".toCharArray());
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    final SSLContext ssl = SSLContext.getInstance("TLS");
    ssl.init(null, tmf.getTrustManagers(), null);

    conn.setSSLSocketFactory(ssl.getSocketFactory());
  }

  protected BasicAuthenticator getBasicAuthenticator() {
    return new BasicAuthenticator("Test Realm") {
      @Override
      public boolean checkCredentials(final String username, final String password) {
        return USERNAME.equals(username) && PASSWORD.equals(password);
      }
    };
  }

  protected void addAuthenticationHeader(final HttpURLConnection con) {
    final String userpass = USERNAME + ":" + PASSWORD;
    final String basicAuth = "Basic " + new String(Base64.encodeBytes(userpass.getBytes()));
    con.setRequestProperty("Authorization", basicAuth);
  }

  protected HttpURLConnection createAndWriteToHttpURLConnection(final String method, final String path, final String entity, final boolean auth) throws IOException {
    final URL url = new URL(path);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();

    if (auth) {
      addAuthenticationHeader(con);
    }

    con.setRequestMethod(method);
    con.setDoOutput(true);
    final OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(entity);
    out.close();
    return con;
  }

  protected HttpsURLConnection createAndWriteToHttpsURLConnection(final String method, final String path, final String entity, final boolean auth) throws Exception {
    final URL url = new URL(path);

    final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

    if (auth) {
      addAuthenticationHeader(con);
    }

    final KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(getClass().getResourceAsStream(KEYSTORE), "password".toCharArray());
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    final SSLContext ssl = SSLContext.getInstance("TLS");
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
