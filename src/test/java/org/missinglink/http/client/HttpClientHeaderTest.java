/*
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

import org.junit.Assert;
import org.junit.Test;
import org.missinglink.http.exception.InvalidUriException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientHeaderTest {

  public HttpClientHeaderTest() {
    super();
  }

  @Test
  public void testNoHeaders() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertEquals(0, httpClient.getHeaders().size());
  }

  @Test
  public void testNullHeader() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").header(null, null).toHttpClient();
    Assert.assertEquals(0, httpClient.getHeaders().size());
  }

  @Test
  public void testNullHeaderValue() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").header("header", null).toHttpClient();
    Assert.assertEquals(1, httpClient.getHeaders().size());
    Assert.assertTrue(httpClient.getHeaders().containsKey("header"));
    Assert.assertNull(httpClient.getHeaders().get("header"));
  }

  @Test
  public void testHeaderValue() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").header("header", "value").toHttpClient();
    Assert.assertEquals(1, httpClient.getHeaders().size());
    Assert.assertTrue(httpClient.getHeaders().containsKey("header"));
    Assert.assertEquals("value", httpClient.getHeaders().get("header"));
  }

  @Test
  public void testAcceptHeader() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").accept("application/xml").toHttpClient();
    Assert.assertEquals(1, httpClient.getHeaders().size());
    Assert.assertTrue(httpClient.getHeaders().containsKey(HttpClient.ACCEPT));
    Assert.assertEquals("application/xml", httpClient.getHeaders().get(HttpClient.ACCEPT));
  }

  @Test
  public void testContentTypeHeader() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").contentType("application/xml").toHttpClient();
    Assert.assertEquals(1, httpClient.getHeaders().size());
    Assert.assertTrue(httpClient.getHeaders().containsKey(HttpClient.CONTENT_TYPE));
    Assert.assertEquals("application/xml", httpClient.getHeaders().get(HttpClient.CONTENT_TYPE));
  }

}
