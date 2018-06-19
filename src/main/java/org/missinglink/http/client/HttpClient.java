/**
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

package org.missinglink.http.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.missinglink.http.encoding.Base64;
import org.missinglink.http.exception.HttpCertificateException;
import org.missinglink.http.exception.HttpInvocationException;
import org.missinglink.http.exception.InvalidStreamException;
import org.missinglink.http.exception.InvalidUriException;
import org.missinglink.tools.StreamUtils;

/**
 * HTTP client which wraps core Java classes {@link URL},
 * {@link HttpURLConnection} and {@link HttpsURLConnection} for communication
 * functionality.
 * <p>
 * Supports TLS/SSL connections per connection (as opposed to a JVM-wide
 * configuration based on the System Property -Djavax.net.ssl.trustStore) using
 * javax.net.ssl.* classes and {@link KeyStore} instances managed in-memory.
 * </p>
 *
 * @author alex.sherwin
 *
 */
public class HttpClient {

  private static final String UTF_8 = "UTF-8";

  // HTTP protocols
  public static final String HTTP = "http";
  public static final String HTTPS = "https";

  // HTTP headers
  public static final String ACCEPT = "Accept";
  public static final String CONTENT_TYPE = "Content-Type";

  private String protocol;
  private String host;
  private Integer port;
  private String path;
  private HttpMethod method = HttpMethod.GET;
  private String username;
  private String password;
  private InputStream entity;
  private boolean binaryEntity = false;
  private boolean followRedirects = true;
  private boolean setContentLength = false;
  private InputStream keyStore;
  private String keyStorePassword;
  private boolean trustAll = false;

  private final Map<String, String> queryUnencoded = new LinkedHashMap<String, String>();
  private final Map<String, String> queryEncoded = new LinkedHashMap<String, String>();

  private final Map<String, String> headers = new HashMap<String, String>();

  protected HttpClient() {
    super();
  }

  /**
   * Start building a {@link HttpClient} instance.
   *
   * @param uri String
   * @return Returns the {@link HttpClientBuilder}
   * @throws InvalidUriException if uri is incorrect
   */
  public static HttpClientBuilder uri(final String uri) throws InvalidUriException {
    return new HttpClientBuilder(new HttpClient(), uri);
  }

  /**
   * Enter building mode.
   *
   * @return Returns the {@link HttpClientBuilder}
   */
  public HttpClientBuilder build() {
    return new HttpClientBuilder(this);
  }

  /**
   * Return the {@link #entity} {@link InputStream} as a String.
   *
   * @return Convert the request entity as a String
   * @throws IOException on failure
   */
  public String getEntityAsString() throws IOException {
    if (null == entity || entity.available() == 0) {
      return null;
    }
    entity.mark(entity.available());
    final String tmp = StreamUtils.inputStreamToString(entity);
    entity.reset();
    return tmp;
  }

  /**
   * Return the {@link #entity} {@link InputStream} as byte array.
   *
   * @return Convert the request entity as a byte array
   * @throws IOException on failure
   */
  public byte[] getEntityAsByteArray() throws IOException {
    if (null == entity || entity.available() == 0) {
      return null;
    }
    entity.mark(entity.available());
    final byte[] result = StreamUtils.inputStreamToByteArray(entity);
    entity.reset();
    return result;
  }

  private static class TrustAllTrustManager implements TrustManager, X509TrustManager {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public boolean isServerTrusted(final X509Certificate[] certs) {
      return true;
    }

    public boolean isClientTrusted(final X509Certificate[] certs) {
      return true;
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String authType) throws CertificateException {
      return;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String authType) throws CertificateException {
      return;
    }
  }

  /**
   * Invoke the HTTP service represented by this {@link HttpClient}.
   *
   * @return The {@link HttpResponse} for the HTTP invocation
   * @throws HttpInvocationException on failure
   * @throws HttpCertificateException on HTTPS failure
   */
  public HttpResponse invoke() throws HttpInvocationException, HttpCertificateException {
    try {
      final HttpResponse response = new HttpResponse(this);
      final String uri = getUri();
      final URL url = new URL(uri);

      final HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
      httpUrlConnection.setDoInput(true);

      // set method
      httpUrlConnection.setRequestMethod(method.name());

      // follow redirects
      httpUrlConnection.setInstanceFollowRedirects(followRedirects);

      // if HTTPS, check for HTTPS options
      if (HTTPS.equalsIgnoreCase(protocol)) {
        if (trustAll == true) {
          final HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(final String urlHostName, final SSLSession session) {
              return true;
            }
          };

          final TrustManager[] trustAllCerts = new TrustManager[] {new TrustAllTrustManager()};

          // Create the SSL context
          final SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, null);

          ((HttpsURLConnection) httpUrlConnection).setSSLSocketFactory(sc.getSocketFactory());

          // Set the default host name verifier to enable the connection.
          ((HttpsURLConnection) httpUrlConnection).setHostnameVerifier(hv);
        } else {
          if (null != keyStore) {
            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keyStore, null == keyStorePassword ? new char[]{} : keyStorePassword.toCharArray());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            final SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, tmf.getTrustManagers(), null);

            ((HttpsURLConnection) httpUrlConnection).setSSLSocketFactory(ssl.getSocketFactory());
          }
        }
      }

      // if username is set, add BASIC authentication header
      if (null != username && username.length() > 0) {
        final String userpass = username + ":" + (null == password ? "" : password);
        final String basicAuth = "Basic " + Base64.encodeBytes(userpass.getBytes());
        httpUrlConnection.setRequestProperty("Authorization", basicAuth);
      }

      // set headers
      if (null != headers && headers.size() > 0) {
        for (final Entry<String, String> header : headers.entrySet()) {
          httpUrlConnection.setRequestProperty(header.getKey(), header.getValue());
        }
      }

      // if an entity is set, write it to the connection
      if (null != entity) {

        httpUrlConnection.setDoOutput(true);

        // if entity is binary then put raw data into output stream
        if (binaryEntity) {
          httpUrlConnection.getOutputStream().write(getEntityAsByteArray());
          httpUrlConnection.getOutputStream().close();
        } else {
          final OutputStreamWriter writer = new OutputStreamWriter(httpUrlConnection.getOutputStream());
          final String entityAsString = getEntityAsString();
          writer.write(entityAsString);
          writer.close();
        }
      }

      // read the response entity
      try {
        final InputStream responseEntityInputStream = httpUrlConnection.getInputStream();
        if (null != responseEntityInputStream) {
          response.setEntity(StreamUtils.inputStreamToByteArray(responseEntityInputStream));
        }
      } catch (final IOException e) {
        // ignore
      }

      // read the error entity
      final InputStream errorEntityInputStream = httpUrlConnection.getErrorStream();
      if (null != errorEntityInputStream) {
        response.setEntity(StreamUtils.inputStreamToByteArray(errorEntityInputStream));
      }

      response.setStatus(httpUrlConnection.getResponseCode());
      response.setMessage(httpUrlConnection.getResponseMessage());

      response.setContentEncoding(httpUrlConnection.getContentEncoding());
      response.setContentLength(httpUrlConnection.getContentLength());
      response.setContentType(httpUrlConnection.getContentType());
      response.setDate(0L == httpUrlConnection.getDate() ? null : new Date(httpUrlConnection.getDate()));
      response.setExpires(0L == httpUrlConnection.getExpiration() ? null : new Date(httpUrlConnection.getExpiration()));
      response.setLastModified(0L == httpUrlConnection.getLastModified() ? null : new Date(httpUrlConnection.getLastModified()));

      if (null != httpUrlConnection.getHeaderFields() && httpUrlConnection.getHeaderFields().size() > 0) {
        for (final Entry<String, List<String>> entry : httpUrlConnection.getHeaderFields().entrySet()) {
          response.getHeaders().put(entry.getKey(), entry.getValue());
        }
      }

      httpUrlConnection.disconnect();

      return response;
    } catch (final SSLHandshakeException e) {
      throw new HttpCertificateException(e);
    } catch (final Throwable t) {
      throw new HttpInvocationException(t);
    }
  }

  /**
   * Build and return the URI.
   *
   * @return The URI fully constructed and encoded
   */
  public String getUri() {
    final StringBuilder sb = new StringBuilder();
    sb.append(protocol.toLowerCase()).append("://").append(host);
    if (null != port) {
      sb.append(":").append(port);
    }
    if (null != path && path.length() > 0) {
      sb.append(path);
    } else {
      sb.append("/");
    }
    if (queryEncoded.size() > 0) {
      sb.append("?");
      boolean first = true;
      for (final Entry<String, String> qp : queryEncoded.entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append("&");
        }
        sb.append(qp.getKey());
        if (null != qp.getValue()) {
          sb.append("=").append(qp.getValue());
        }
      }
    }
    return sb.toString();
  }

  /**
   * getExtension Return the protocol (http/https).
   *
   * @return The HTTP protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * @return The HTTP host
   */
  public String getHost() {
    return host;
  }

  /**
   * @return The HTTP port
   */
  public Integer getPort() {
    return port;
  }

  /**
   * @return the keyStore
   */
  public InputStream getKeyStore() {
    return keyStore;
  }

  /**
   * @return the keyStorePassword
   */
  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  /**
   * @return the queryUnencoded
   */
  public Map<String, String> getQueryUnencoded() {
    return queryUnencoded;
  }

  /**
   * @return the queryEncoded
   */
  public Map<String, String> getQueryEncoded() {
    return queryEncoded;
  }

  /**
   * @return the headers
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * @return the method
   */
  public HttpMethod getMethod() {
    return method;
  }

  /**
   * @return if instance follows redirects
   */
  public boolean getFollowRedirects() {
    return followRedirects;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return the entity
   */
  public InputStream getEntity() {
    return entity;
  }

  /**
   * {@link HttpClient} builder.
   *
   * @author alex.sherwin
   */
  public static class HttpClientBuilder {

    protected final HttpClient httpClient;

    protected HttpClientBuilder(final HttpClient httpClient) {
      this.httpClient = httpClient;
    }

    protected HttpClientBuilder(final HttpClient httpClient, final String uri) throws InvalidUriException {
      super();
      this.httpClient = httpClient;
      parseUri(uri);
    }

    /**
     * Parse the URI into its components.
     *
     * @param uri String
     * @throws InvalidUriException
     *           On any failure/invalid URI
     */
    protected void parseUri(final String uri) throws InvalidUriException {
      try {
        final URI jnUri = new URI(fixup(uri));
        httpClient.protocol = jnUri.getScheme().toLowerCase();
        httpClient.host = jnUri.getHost();
        httpClient.port = jnUri.getPort() != -1 ? jnUri.getPort() : null;
        httpClient.path = jnUri.getRawPath();
        parseQuery(jnUri.getRawQuery());
        if (!httpClient.protocol.equals("http") && !httpClient.protocol.equals("https")) {
          throw new InvalidUriException(uri);
        }
      } catch (final Throwable t) {
        throw new InvalidUriException(t.getMessage() + " URI: " + uri, t);
      }
    }

    /**
     * Parse a query string into its components, split components by "&amp;" and
     * their key, value parts with "="
     *
     * @param query String
     */
    protected void parseQuery(final String query) {
      if (null != query && query.length() > 0) {
        final String[] queryParts = query.split("&");
        for (final String queryPart : queryParts) {
          final String[] keyValue = queryPart.split("=", 2);
          httpClient.queryEncoded.put(keyValue[0],
                  keyValue.length > 1 && keyValue[1].length() > 0 ? keyValue[1] : null);
        }
      }
      for (final Entry<String, String> entry : httpClient.queryEncoded.entrySet()) {
        httpClient.queryUnencoded.put(entry.getKey(), decode(entry.getValue()));
      }
    }

    protected String encode(final String str) {
      try {
        return null == str ? null : URLEncoder.encode(str, UTF_8);
      } catch (final UnsupportedEncodingException e) {
        return null;
      }
    }

    protected String decode(final String str) {
      try {
        return null == str ? null : URLDecoder.decode(str, UTF_8);
      } catch (final UnsupportedEncodingException e) {
        return null;
      }
    }

    /**
     * Encode any blatantly unusable chars, leave reserved chars
     * (<code>&amp;/:;=?@</code>) as-is. This is not a normal full encode - it is
     * just a fix-up to try to make questionable URLs usable.
     *
     * @param str String an URL
     * @return String encoded URL except for reserved characters
     */
    protected String fixup(final String str) {
      if (null == str) {
        return null;
      }
      try {
        return URLEncoder.encode(str, UTF_8)
            .replace("%26", "&")
            .replace("%2F", "/")
            .replace("%3A", ":")
            .replace("%3B", ";")
            .replace("%3D", "=")
            .replace("%3F", "?")
            .replace("%40", "@")
            .replace("%25", "%");
      } catch (final UnsupportedEncodingException e) {
        return null;
      }
    }

    /**
     * Return the current {@link HttpClient} that this builder represents.
     *
     * @return The {@link HttpClient} this {@link HttpClientBuilder} represents.
     */
    public HttpClient toHttpClient() {
      return httpClient;
    }

    /**
     * Add a query parameter to the {@link HttpClient}. The value can be null,
     * and is null safe for null params (no-op).
     *
     * @param param String
     * @param value String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder query(final String param, final String value) {
      if (null != param && param.length() > 0) {
        httpClient.queryUnencoded.put(param, value);
        httpClient.queryEncoded.put(param, encode(value));
      }
      return this;
    }

    /**
     * Add a header to the {@link HttpClient}. The value can be null, and is
     * null safe for header values (no-op).
     *
     * @param header String
     * @param value String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder header(final String header, final String value) {
      if (null != header && header.length() > 0) {
        httpClient.headers.put(header, value);
      }
      return this;
    }

    /**
     * Add an "Accept" header to the {@link HttpClient}.
     *
     * @param value String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder accept(final String value) {
      httpClient.headers.put(ACCEPT, value);
      return this;
    }

    /**
     * Add an "Content-Type" header to the {@link HttpClient}.
     *
     * @param value String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder contentType(final String value) {
      httpClient.headers.put(CONTENT_TYPE, value);
      return this;
    }

    /**
     * Set the method on the {@link HttpClient}.
     *
     * @param method HttpMethod
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder method(final HttpMethod method) {
      httpClient.method = method;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#GET}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder get() {
      httpClient.method = HttpMethod.GET;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#POST}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder post() {
      httpClient.method = HttpMethod.POST;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#PUT}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder put() {
      httpClient.method = HttpMethod.PUT;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#TRACE}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder trace() {
      httpClient.method = HttpMethod.TRACE;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#OPTIONS}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder options() {
      httpClient.method = HttpMethod.OPTIONS;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#DELETE}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder delete() {
      httpClient.method = HttpMethod.DELETE;
      return this;
    }

    /**
     * Set the method on the {@link HttpClient} to {@link HttpMethod#HEAD}.
     *
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder head() {
      httpClient.method = HttpMethod.HEAD;
      return this;
    }

    /**
     * Set the authentication credentials to use on the {@link HttpClient}.
     *
     * @param username String
     * @param password String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder credentials(final String username, final String password) {
      httpClient.username = username;
      httpClient.password = password;
      return this;
    }

    /**
     * Set the request entity on the {@link HttpClient}.
     *
     * @param is InputStream
     * @param binary boolean
     *          tell whether or not the entity has to be considered as binary
     *          stream
     * @return The new {@link HttpClientBuilder}
     * @throws InvalidStreamException on failure
     */
    public HttpClientBuilder entity(final InputStream is, final boolean binary) throws InvalidStreamException {
      if (null != is && !is.markSupported()) {
        throw new InvalidStreamException("InputStream of type [" + is.getClass().getName() + "] does not support marking");
      }
      httpClient.entity = is;
      httpClient.binaryEntity = binary;
      if (httpClient.setContentLength) {
        try {
          header("Content-Length", "" + is.available());
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
      return this;
    }

    /**
     * Set the request entity on the {@link HttpClient}. This method is a
     * wrapper to {@link #entity(InputStream, boolean)} with binary set to false
     *
     * @param is InputStream
     * @return The new {@link HttpClientBuilder}
     * @throws InvalidStreamException on failure
     */
    public HttpClientBuilder entity(final InputStream is) throws InvalidStreamException {
      return entity(is, false);
    }

    /**
     * Calls {@link #entity(InputStream)} with str wrapped in a
     * {@link ByteArrayInputStream}.
     *
     * @param str String
     * @param binary boolean
     *          tell whether or not the entity has to be considered as binary
     *          stream
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder entity(final String str, final boolean binary) {
      if (null != str) {
        try {
          entity(new ByteArrayInputStream(str.getBytes()), binary);
        } catch (final InvalidStreamException e) {
          // should never happen
          e.printStackTrace();
        }
      }
      return this;
    }

    /**
     * Calls {@link #entity(String, boolean)} with binary set to false
     *
     * @param str String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder entity(final String str) {
      return entity(str, false);
    }

    /**
     * Set the {@link InputStream} to use when creating a {@link KeyStore}
     *
     * @param is InputStream
     * @param password String
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder keyStore(final InputStream is, final String password) {
      return keyStore(is, password, false);
    }

    /**
     * Set the {@link InputStream} to use when creating a {@link KeyStore}
     *
     * @param is InputStream
     * @param password String
     * @param trustAll boolean
     * @return The new {@link HttpClientBuilder}
     */
    public HttpClientBuilder keyStore(final InputStream is, final String password, final boolean trustAll) {
      httpClient.keyStore = is;
      httpClient.keyStorePassword = password;
      httpClient.trustAll = trustAll;
      return this;
    }

    public HttpClientBuilder followRedirects(final boolean follow) {
      httpClient.followRedirects = follow;
      return this;
    }

    public HttpClientBuilder setContentLength(final boolean setHeader) {
      httpClient.setContentLength = setHeader;
      return this;
    }
  }

}
