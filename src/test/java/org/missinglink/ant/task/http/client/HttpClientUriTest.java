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

package org.missinglink.ant.task.http.client;

import org.junit.Assert;
import org.junit.Test;
import org.missinglink.http.client.HttpClient;
import org.missinglink.http.exception.InvalidUriException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientUriTest {

  public HttpClientUriTest() {
    super();
  }

  @Test
  public void testValidHttpUris() throws InvalidUriException {
    HttpClient.uri("http://host");
    HttpClient.uri("http://host/");
    HttpClient.uri("http://host:80");
    HttpClient.uri("http://host:80/");
    HttpClient.uri("http://host/context");
    HttpClient.uri("http://host:80/context");
    HttpClient.uri("http://host:80/context?query=value");
    HttpClient.uri("http://host:80/context/?query=value");
    HttpClient.uri("http://host:80/context/file.html");
    HttpClient.uri("http://host:80/context/file.html?query=value");
    HttpClient.uri("http://host:80/context/file.html?query=value");
    HttpClient.uri("http://host:80/context.with/periods./here/file-1.0.02.html?query=value");
  }

  @Test
  public void testValidHttpsUris() throws InvalidUriException {
    HttpClient.uri("https://host");
    HttpClient.uri("https://host/");
    HttpClient.uri("https://host:443");
    HttpClient.uri("https://host:443/");
    HttpClient.uri("https://host/context");
    HttpClient.uri("https://host:443/context");
    HttpClient.uri("https://host:443/context?query=value");
    HttpClient.uri("https://host:443/context/?query=value");
    HttpClient.uri("https://host:443/context/file.html");
    HttpClient.uri("https://host:443/context/file.html?query=value");
    HttpClient.uri("https://host:443/context/file.html?query=value");
    HttpClient.uri("https://host:443/context.with/periods./here/file-1.0.02.html?query=value");
  }

  @Test(expected = InvalidUriException.class)
  public void testInvalidProtocolUri() throws Exception {
    HttpClient.uri("htt://host");
  }

  @Test
  public void testUriNoBuilderMethods() throws InvalidUriException {
    HttpClient client = HttpClient.uri("http://host").toHttpClient();
    Assert.assertEquals("http://host/", client.getUri());
    client = HttpClient.uri("http://host/").toHttpClient();
    Assert.assertEquals("http://host/", client.getUri());
    client = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertEquals("http://host/context", client.getUri());
    client = HttpClient.uri("http://host/context/longer").toHttpClient();
    Assert.assertEquals("http://host/context/longer", client.getUri());
    client = HttpClient.uri("http://host/context/longer/").toHttpClient();
    Assert.assertEquals("http://host/context/longer/", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html", client.getUri());
    client = HttpClient.uri("http://host/context/longer/?qp").toHttpClient();
    Assert.assertEquals("http://host/context/longer/?qp", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html?qp").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html?qp", client.getUri());
    client = HttpClient.uri("http://host/context/longer/?qp=").toHttpClient();
    Assert.assertEquals("http://host/context/longer/?qp", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html?qp=").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html?qp", client.getUri());
    client = HttpClient.uri("http://host/context/longer/?qp=value").toHttpClient();
    Assert.assertEquals("http://host/context/longer/?qp=value", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html?qp=value").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html?qp=value", client.getUri());
    client = HttpClient.uri("http://host/context/longer/?qp=value with spaces").toHttpClient();
    Assert.assertEquals("http://host/context/longer/?qp=value+with+spaces", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html?qp=value with spaces").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html?qp=value+with+spaces", client.getUri());
    client = HttpClient.uri("http://host/context/longer/?qp=value with spaces&qp2=value2").toHttpClient();
    Assert.assertEquals("http://host/context/longer/?qp2=value2&qp=value+with+spaces", client.getUri());
    client = HttpClient.uri("http://host/context/longer/file.html?qp=value with spaces&qp2=value2").toHttpClient();
    Assert.assertEquals("http://host/context/longer/file.html?qp2=value2&qp=value+with+spaces", client.getUri());
  }
}
