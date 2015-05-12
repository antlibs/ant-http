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
import org.missinglink.http.client.HttpMethod;
import org.missinglink.http.exception.InvalidUriException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientMethodTest {

  public HttpClientMethodTest() {
    super();
  }

  @Test
  public void testDefaultMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").toHttpClient();
    Assert.assertEquals(HttpMethod.GET, httpClient.getMethod());
  }

  @Test
  public void testGetMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").get().toHttpClient();
    Assert.assertEquals(HttpMethod.GET, httpClient.getMethod());
  }

  @Test
  public void testPostMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").post().toHttpClient();
    Assert.assertEquals(HttpMethod.POST, httpClient.getMethod());
  }

  @Test
  public void testPutMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").put().toHttpClient();
    Assert.assertEquals(HttpMethod.PUT, httpClient.getMethod());
  }

  @Test
  public void testHeadMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").head().toHttpClient();
    Assert.assertEquals(HttpMethod.HEAD, httpClient.getMethod());
  }

  @Test
  public void testOptionsMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").options().toHttpClient();
    Assert.assertEquals(HttpMethod.OPTIONS, httpClient.getMethod());
  }

  @Test
  public void testTraceMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").trace().toHttpClient();
    Assert.assertEquals(HttpMethod.TRACE, httpClient.getMethod());
  }

  @Test
  public void testDeleteMethod() throws InvalidUriException {
    final HttpClient httpClient = HttpClient.uri("http://host/context").delete().toHttpClient();
    Assert.assertEquals(HttpMethod.DELETE, httpClient.getMethod());
  }

}
