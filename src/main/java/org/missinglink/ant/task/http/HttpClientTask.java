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

package org.missinglink.ant.task.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.missinglink.http.client.HttpClient;
import org.missinglink.http.client.HttpClient.HttpClientBuilder;
import org.missinglink.http.client.HttpMethod;
import org.missinglink.http.client.HttpResponse;
import org.missinglink.http.exception.HttpCertificateException;
import org.missinglink.http.exception.HttpInvocationException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientTask extends Task {

  // ant parameters
  private String url;
  private String statusProperty;
  private String entityProperty;
  private File outFile;
  private HttpMethod method;
  private CredentialsNode credentials;
  private KeyStoreNode keystore;
  private EntityNode entity;
  private HeadersNode headers;
  private QueryNode query;
  private boolean printRequest;
  private boolean printResponse;
  @SuppressWarnings("unused")
  private boolean printRequestHeaders = true;
  @SuppressWarnings("unused")
  private boolean printResponseHeaders = true;
  private int expected = 200;
  private boolean failOnUnexpected = true;
  private boolean followRedirects = true;
  private boolean setContentLengthHeader = false;
  private boolean update = true;
  private int logLevel = Project.MSG_INFO;

  // http task parameters
  private HttpClient httpClient;

  public HttpClientTask() {
    super();
  }

  @Override
  public void init() {
    super.init();
    // Issue 13 - Get the log level at startup so we can use it later to change
    // the log messages
    logLevel = getLogLevel();
  }

  @Override
  public void execute() {
    super.execute();

    // setup HttpClient
    initHttpClient();

    if (!outputIsAvailable()) {
      log("Output Entity file " + safeOutFilename() + " already exists, update is set to " + update + ", skipping HTTP request", Project.MSG_INFO);
      return;
    }

    // invoke HttpClient
    HttpResponse response = null;

    log("********************", Project.MSG_VERBOSE);
    log("HTTP Request", Project.MSG_VERBOSE);
    log("********************", Project.MSG_VERBOSE);

    final String uri = httpClient.getUri();

    log("URL:\t\t" + uri, Project.MSG_VERBOSE);
    log("Method:\t\t" + httpClient.getMethod().name(), Project.MSG_VERBOSE);

    if (isInfo()) {
      log("HTTP " + httpClient.getMethod().name() + " " + uri, Project.MSG_INFO);
    }

    if (null != credentials && credentials.isValid()) {
      log("Credentials:\t" + (credentials.isShow() ? credentials.getUsername() + " / " + credentials.getPassword() : "[hidden]"), Project.MSG_VERBOSE);
    }
    if (httpClient.getHeaders().size() > 0 && printRequestHeaders) {
      log("Headers:\t\tyes", Project.MSG_VERBOSE);
      for (final Entry<String, String> entry : httpClient.getHeaders().entrySet()) {
        log("\t" + entry.getKey() + ": " + entry.getValue());
      }
    } else {
      log("Headers:\t\tno", Project.MSG_VERBOSE);
    }
    if (httpClient.getQueryUnencoded().size() > 0) {
      log("Query Parameters:\tyes", Project.MSG_VERBOSE);
      for (final Entry<String, String> entry : httpClient.getQueryUnencoded().entrySet()) {
        log("\t" + entry.getKey() + "=" + entry.getValue(), Project.MSG_VERBOSE);
      }
    } else {
      log("Query Parameters:\tno", Project.MSG_VERBOSE);
    }
    if (printRequest) {
      log("Entity:\t\t" + (null == httpClient.getEntity() ? "no" : "yes"), Project.MSG_INFO);
    } else {
      log("Entity:\t\t" + (null == httpClient.getEntity() ? "no" : "yes"), Project.MSG_VERBOSE);
    }
    if (null != httpClient.getEntity() && printRequest) {
      try {
        log("------ BEGIN ENTITY ------", Project.MSG_INFO);
        log(httpClient.getEntityAsString(), Project.MSG_INFO);
        log("------- END ENTITY -------", Project.MSG_INFO);
      } catch (final IOException e) {
        log(e, Project.MSG_ERR);
        throw new BuildException(e);
      }
    }

    try {
      response = httpClient.invoke();
    } catch (final HttpInvocationException e) {
      throw new BuildException(e);
    } catch (final HttpCertificateException e) {
      throw new BuildException(e);
    } catch (final Throwable t) {
      throw new BuildException(t);
    }

    if (null != response) {

      // Issue 21 - Write status to a property
      if (null != getStatusProperty() && getStatusProperty().length() > 0) {
        getProject().setProperty(getStatusProperty(), Integer.toString(response.getStatus()));
      }

      log("", Project.MSG_VERBOSE);
      log("********************", Project.MSG_VERBOSE);
      log("HTTP Response", Project.MSG_VERBOSE);
      log("********************", Project.MSG_VERBOSE);
      log("Status:\t\t" + response.getStatus(), Project.MSG_VERBOSE);

      if (isInfo()) {
        log("Response Status: " + response.getStatus(), Project.MSG_INFO);
      }

      if (response.getHeaders().size() > 0) {
        log("Headers:\t\tyes", Project.MSG_VERBOSE);
        for (final Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
          for (final String value : entry.getValue()) {
            if (null == entry.getKey()) {
              log("\t" + value, Project.MSG_VERBOSE);
            } else {
              log("\t" + entry.getKey() + ": " + value, Project.MSG_VERBOSE);
            }
          }
        }
      } else {
        log("Headers:\t\tno");
      }

      final boolean responseHasEntity = null != response.getEntity();
      if (null == outFile) {
        if (printResponse) {
          log("Entity:\t\t" + (null == response.getEntity() ? "no" : "yes"), Project.MSG_INFO);
        } else {
          log("Entity:\t\t" + (null == response.getEntity() ? "no" : "yes"), Project.MSG_VERBOSE);
        }
        if (responseHasEntity) {
          final String respEntity = response.getEntityAsString();

          // Issue 21 - Write entity to a property
          if (null != getEntityProperty() && getEntityProperty().length() > 0) {
            getProject().setProperty(getEntityProperty(), respEntity);
          }

          if (printResponse) {
            log("------ BEGIN ENTITY ------", Project.MSG_INFO);
            log(respEntity, Project.MSG_INFO);
            log("------- END ENTITY -------", Project.MSG_INFO);
          }
        }
      } else if (responseHasEntity) {
        try {
          mkdirs(outFile);
          final OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile));
          fos.write(response.getEntity());
          fos.flush();
          fos.close();
          log("Entity written to file:\t" + outFile.getAbsolutePath(), Project.MSG_INFO);
        } catch (final Throwable t) {
          throw new BuildException("Failed to write response entity to file: " + outFile.getAbsolutePath() + " - " + t.getMessage(), t);
        }
      }

      if (response.getStatus() != expected && failOnUnexpected) {
        throw new BuildException("Expected Status [" + expected + "] but got [" + response.getStatus() + "] for URI [" + uri + "]");
      }
    }
  }

  // Issue 13 - This can be used to determine the current logging level,
  // allowing for different logs altogether for INFO/VERBOSE
  protected int getLogLevel() {
    try {
      for (final Object listener : getProject().getBuildListeners()) {
        if (DefaultLogger.class.equals(listener.getClass())) {
          final Field msgOutputLevel = listener.getClass().getDeclaredField("msgOutputLevel");
          msgOutputLevel.setAccessible(true); // there is no spoon
          return msgOutputLevel.getInt(listener);
        }
      }
    } catch (final Exception e) {
      log("Unable to determine the Ant log level, please report the issue to the project at http://code.google.com/p/missing-link/issues", Project.MSG_WARN);
    }
    return Project.MSG_INFO;
  }

  /**
   * For use to fix Issue 13, returns true only if the log level is
   * {@link Project#MSG_INFO}
   *
   * @return boolean
   */
  protected boolean isInfo() {
    return logLevel == Project.MSG_INFO;
  }

  /**
   * For use to fix Issue 13, returns true only if the log level is
   * {@link Project#MSG_VERBOSE} or {@link Project#MSG_DEBUG}
   *
   * @return boolean
   */
  protected boolean isVerbose() {
    return logLevel == Project.MSG_VERBOSE || logLevel == Project.MSG_DEBUG;
  }

  protected boolean outputIsAvailable() {
    return outFile == null || !outFile.exists() || update;
  }

  protected String safeOutFilename() {
    return null != outFile ? outFile.getAbsolutePath() : "";
  }

  protected static void mkdirs(final File file) {
    final File dir = file.getParentFile();
    if (!dir.exists()) {
      final boolean mkdirs = dir.mkdirs();
      if (!mkdirs) {
        throw new BuildException("Could not make directories for " + dir.getAbsolutePath());
      }
    }
  }

  protected void initHttpClient() {
    try {
      HttpClientBuilder builder = HttpClient.uri(url);

      // set method, default to GET
      builder = builder.method(null == method ? HttpMethod.GET : method);

      // set credentials
      if (null != credentials && credentials.isValid()) {
        builder = builder.credentials(credentials.getUsername(), credentials.getPassword());
      }

      // set keystore
      if (null != keystore && keystore.isValid()) {
        builder = builder.keyStore(new FileInputStream(keystore.getFile()), keystore.getPassword());
      }

      // set headers
      if (null != headers && headers.isValid()) {
        for (final HeaderNode header : headers.getHeaders()) {
          builder = builder.header(header.getName(), header.getValue());
        }
      }

      if (!followRedirects) {
        builder.followRedirects(followRedirects);
      }

      // set query parameters
      if (null != query && query.isValid()) {
        for (final QueryParameterNode qp : query.getParameters()) {
          builder = builder.query(qp.getName(), qp.getValue());
        }
      }

      // set request entity
      builder.setContentLength(setContentLengthHeader);
      if (null != entity && entity.isValid()) {
        if (null != entity.getFile()) {
          // 1. prefer file
          final FileInputStream is = new FileInputStream(entity.getFile());
          final ByteArrayOutputStream os = new ByteArrayOutputStream();
          final byte[] buf = new byte[1024];
          try {
            for (int num; (num = is.read(buf)) != -1;) {
              os.write(buf, 0, num);
            }
          } catch (final IOException e) {
            throw new BuildException(e);
          } finally {
            is.close();
          }

          builder = builder.entity(new ByteArrayInputStream(os.toByteArray()), entity.getBinary());
        } else if (null != entity.getValue() && entity.getValue().length() > 0) {
          // 2. prefer value attribute
          builder = builder.entity(entity.getValue());
        } else {
          // 3. fall back to text content
          builder = builder.entity(getProject().replaceProperties(entity.getText()), entity.getBinary());
        }
      }

      httpClient = builder.toHttpClient();

    } catch (final Throwable t) {
      throw new BuildException(t);
    }
  }

  public void addConfiguredCredentials(final CredentialsNode credentials) {
    this.credentials = credentials;
  }

  public void addConfiguredKeystore(final KeyStoreNode keystore) {
    this.keystore = keystore;
  }

  public void addConfiguredEntity(final EntityNode entity) {
    this.entity = entity;
  }

  public void addConfiguredHeaders(final HeadersNode headers) {
    this.headers = headers;
  }

  public void addConfiguredQuery(final QueryNode query) {
    this.query = query;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public File getOutFile() {
    return outFile;
  }

  public void setOutFile(final File outFile) {
    this.outFile = outFile;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(final HttpMethod method) {
    this.method = method;
  }

  public void setPrintRequest(final boolean printRequest) {
    this.printRequest = printRequest;
  }

  public void setPrintResponse(final boolean printResponse) {
    this.printResponse = printResponse;
  }

  public void setExpected(final int expected) {
    this.expected = expected;
  }

  public void setFailOnUnexpected(final boolean failOnUnexpected) {
    this.failOnUnexpected = failOnUnexpected;
  }

  public void setFollowRedirects(final boolean followRedirects) {
    this.followRedirects = followRedirects;
  }

  public void setPrintResponseHeaders(final boolean printResponseHeaders) {
    this.printResponseHeaders = printResponseHeaders;
  }

  public void setPrintRequestHeaders(final boolean printRequestHeaders) {
    this.printRequestHeaders = printRequestHeaders;
  }

  public void setSetContentLengthHeader(final boolean setHeader) {
    this.setContentLengthHeader = setHeader;
  }

  public String getStatusProperty() {
    return statusProperty;
  }

  public void setStatusProperty(final String statusProperty) {
    this.statusProperty = statusProperty;
  }

  public void setUpdate(final boolean update) {
    this.update = update;
  }

  public String getEntityProperty() {
    return entityProperty;
  }

  public void setEntityProperty(final String entityProperty) {
    this.entityProperty = entityProperty;
  }
}
