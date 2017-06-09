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

import org.junit.Assert;
import org.junit.Test;
import org.missinglink.http.exception.InvalidUriException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientQueryTest {

  public HttpClientQueryTest() {
    super();
  }

  @Test
  public void testNoQuery() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().size() == 0);
    Assert.assertTrue(httpClient.getQueryUnencoded().size() == 0);
  }

  @Test
  public void testNoQueryInUriNullParam() throws InvalidUriException {
    HttpClient httpClient = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().size() == 0);
    Assert.assertTrue(httpClient.getQueryUnencoded().size() == 0);
    httpClient = httpClient.build().query(null, null).toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().size() == 0);
    Assert.assertTrue(httpClient.getQueryUnencoded().size() == 0);
  }

  @Test
  public void testNoQueryInUriNullValue() throws InvalidUriException {
    HttpClient httpClient = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().size() == 0);
    Assert.assertTrue(httpClient.getQueryUnencoded().size() == 0);
    httpClient = httpClient.build().query("qp", null).toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertNull(httpClient.getQueryEncoded().get("qp"));
    Assert.assertNull(httpClient.getQueryUnencoded().get("qp"));
  }

  @Test
  public void testQueryNoValue() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context?qp").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertNull(httpClient.getQueryUnencoded().get("qp"));
    Assert.assertNull(httpClient.getQueryEncoded().get("qp"));
  }

  @Test
  public void testQueryValue() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context?qp=value").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertEquals("value", httpClient.getQueryUnencoded().get("qp"));
    Assert.assertEquals("value", httpClient.getQueryEncoded().get("qp"));
  }

  @Test
  public void testQueryAndBuilderValue() throws InvalidUriException {
    HttpClient httpClient = HttpClient.uri("http://host/context?qp=value").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertEquals("value", httpClient.getQueryUnencoded().get("qp"));
    Assert.assertEquals("value", httpClient.getQueryEncoded().get("qp"));
    httpClient = httpClient.build().query("qp2", "value").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp2"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp2"));
    Assert.assertEquals("value", httpClient.getQueryUnencoded().get("qp2"));
    Assert.assertEquals("value", httpClient.getQueryEncoded().get("qp2"));
  }

  @Test
  public void testQueryValueAndBuilderEncoded() throws InvalidUriException {
    HttpClient httpClient = HttpClient.uri("http://host/context?qp=value space").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertEquals("value space", httpClient.getQueryUnencoded().get("qp"));
    Assert.assertEquals("value+space", httpClient.getQueryEncoded().get("qp"));
    httpClient = httpClient.build().query("qp2", "qp2 space").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp2"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp2"));
    Assert.assertEquals("qp2 space", httpClient.getQueryUnencoded().get("qp2"));
    Assert.assertEquals("qp2+space", httpClient.getQueryEncoded().get("qp2"));
  }

  @Test
  public void testQueryMultipleValues() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context?qp=value&qp2=value2").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp2"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp2"));
    Assert.assertEquals("value", httpClient.getQueryUnencoded().get("qp"));
    Assert.assertEquals("value", httpClient.getQueryEncoded().get("qp"));
    Assert.assertEquals("value2", httpClient.getQueryUnencoded().get("qp2"));
    Assert.assertEquals("value2", httpClient.getQueryEncoded().get("qp2"));
  }

  @Test
  public void testQueryMultipleValuesEncoded() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context?qp=value space&qp2=value2 space").toHttpClient();
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp"));
    Assert.assertTrue(httpClient.getQueryEncoded().containsKey("qp2"));
    Assert.assertTrue(httpClient.getQueryUnencoded().containsKey("qp2"));
    Assert.assertEquals("value space", httpClient.getQueryUnencoded().get("qp"));
    Assert.assertEquals("value+space", httpClient.getQueryEncoded().get("qp"));
    Assert.assertEquals("value2 space", httpClient.getQueryUnencoded().get("qp2"));
    Assert.assertEquals("value2+space", httpClient.getQueryEncoded().get("qp2"));
  }

}
