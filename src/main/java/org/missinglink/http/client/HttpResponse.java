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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps the response of an HTTP request.
 *
 * @author alex.sherwin
 *
 */
public class HttpResponse {

  private HttpClient httpClient;

  private int status;
  private String message;

  private byte[] entity;

  private String contentEncoding;
  private String contentType;
  private int contentLength;
  private Date date;
  private Date expires;
  private Date lastModified;

  private Map<String, List<String>> headers = new HashMap<String, List<String>>();

  public HttpResponse(final HttpClient httpClient) {
    super();
    this.httpClient = httpClient;
  }

  public List<String> getHeader(final String header) {
    if (headers.containsKey(header)) {
      return headers.get(header);
    }
    return null;
  }

  public String getHeaderSingleValue(final String header) {
    if (headers.containsKey(header) && null != headers.get(header) && headers.get(header).size() > 0) {
      return headers.get(header).get(0);
    }
    return null;
  }

  /**
   * @return the headers
   */
  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  /**
   * @param headers
   *          the headers to set
   */
  public void setHeaders(final Map<String, List<String>> headers) {
    this.headers = headers;
  }

  /**
   * @return the expires
   */
  public Date getExpires() {
    return expires;
  }

  /**
   * @param expires
   *          the expires to set
   */
  public void setExpires(final Date expires) {
    this.expires = expires;
  }

  /**
   * @return the lastModified
   */
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * @param lastModified
   *          the lastModified to set
   */
  public void setLastModified(final Date lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * @return the httpClient
   */
  public HttpClient getHttpClient() {
    return httpClient;
  }

  /**
   * @param httpClient
   *          the httpClient to set
   */
  public void setHttpClient(final HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(final int status) {
    this.status = status;
  }

  /**
   * @return the responseEntity
   */
  public byte[] getEntity() {
    return entity;
  }

  public String getEntityAsString() {
    if (null != entity && entity.length > 0) {
      return new String(entity);
    }
    return null;
  }

  /**
   * @param responseEntity
   *          the responseEntity to set
   */
  public void setEntity(final byte[] responseEntity) {
    this.entity = responseEntity;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage(final String message) {
    this.message = message;
  }

  /**
   * @return the contentEncoding
   */
  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * @param contentEncoding
   *          the contentEncoding to set
   */
  public void setContentEncoding(final String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *          the contentType to set
   */
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  /**
   * @return the contentLength
   */
  public int getContentLength() {
    return contentLength;
  }

  /**
   * @param contentLength
   *          the contentLength to set
   */
  public void setContentLength(final int contentLength) {
    this.contentLength = contentLength;
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date
   *          the date to set
   */
  public void setDate(final Date date) {
    this.date = date;
  }

}
