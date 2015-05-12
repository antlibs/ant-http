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
public class HttpClientAuthenticationDataTest {

  public HttpClientAuthenticationDataTest() {
    super();
  }

  @Test
  public void testSetNullUsername() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").credentials(null, null).toHttpClient();
    Assert.assertNull(httpClient.getUsername());
  }

  @Test
  public void testSetNullPassword() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").credentials(null, null).toHttpClient();
    Assert.assertNull(httpClient.getPassword());
  }

  @Test
  public void testSetUsername() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").credentials("user", null).toHttpClient();
    Assert.assertEquals("user", httpClient.getUsername());
  }

  @Test
  public void testSetPassword() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").credentials(null, "passwd").toHttpClient();
    Assert.assertEquals("passwd", httpClient.getPassword());
  }

  @Test
  public void testSetUserAndPassword() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").credentials("user", "passwd").toHttpClient();
    Assert.assertEquals("user", httpClient.getUsername());
    Assert.assertEquals("passwd", httpClient.getPassword());
  }

  @Test
  public void testUnsetUserAndPassword() throws InvalidUriException {
    HttpClient httpClient = HttpClient.uri("http://host/context").credentials("user", "passwd").toHttpClient();
    Assert.assertEquals("user", httpClient.getUsername());
    Assert.assertEquals("passwd", httpClient.getPassword());
    httpClient = httpClient.build().credentials(null, null).toHttpClient();
    Assert.assertNull(httpClient.getUsername());
    Assert.assertNull(httpClient.getPassword());
  }
}
