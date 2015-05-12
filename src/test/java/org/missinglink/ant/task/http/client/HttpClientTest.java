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

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.missinglink.ant.task.http.server.AbstractHttpServerTest;
import org.missinglink.http.client.HttpClient;
import org.missinglink.http.client.HttpResponse;
import org.missinglink.http.exception.HttpClientException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientTest extends AbstractHttpServerTest {

  public HttpClientTest() {
    super();
  }

  @Before
  public void before() throws IOException {
    startHttpServer();
  }

  @After
  public void after() throws IOException {
    stopHttpServer();
  }

  @Test
  public void testGetWithEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + PING_CONTEXT).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(PING_RESPONSE.getBytes(), response.getEntity());
    Assert.assertEquals(PING_RESPONSE, response.getEntityAsString());
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void testGetSecureWithEntityAuthFailure() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + SECURE_CONTEXT + PING_CONTEXT).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertEquals(401, response.getStatus());
  }

  @Test
  public void testGetSecureWithEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + SECURE_CONTEXT + PING_CONTEXT).credentials(USERNAME, PASSWORD).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(PING_RESPONSE.getBytes(), response.getEntity());
    Assert.assertEquals(PING_RESPONSE, response.getEntityAsString());
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void test404() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + "/doesnt/exist").toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertEquals(404, response.getStatus());
  }

  @Test
  public void test500() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + INTERNAL_SERER_ERROR_CONTEXT).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(INTERNAL_SERER_ERROR_RESPONSE.getBytes(), response.getEntity());
    Assert.assertEquals(500, response.getStatus());
  }

  @Test
  public void test500Secured() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + SECURE_CONTEXT + INTERNAL_SERER_ERROR_CONTEXT).credentials(USERNAME, PASSWORD).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(INTERNAL_SERER_ERROR_RESPONSE.getBytes(), response.getEntity());
    Assert.assertEquals(500, response.getStatus());
  }

  @Test
  public void testPostWithResponseEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + ECHO_CONTEXT).post().entity("Hello World").toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals("Hello World".getBytes(), response.getEntity());
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void testPostSecuredWithResponseEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + SECURE_CONTEXT + ECHO_CONTEXT).post().entity("Hello World").credentials(USERNAME, PASSWORD).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals("Hello World".getBytes(), response.getEntity());
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void testPutWithResponseEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + ECHO_CONTEXT).put().entity("Hello World").toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals("Hello World".getBytes(), response.getEntity());
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void testPutSecuredWithResponseEntity() throws HttpClientException, IOException {
    final HttpClient httpClient = HttpClient.uri(getHttpServerUri() + SECURE_CONTEXT + ECHO_CONTEXT).put().entity("Hello World").credentials(USERNAME, PASSWORD).toHttpClient();
    final HttpResponse response = httpClient.invoke();
    Assert.assertNotNull(response);
    Assert.assertArrayEquals("Hello World".getBytes(), response.getEntity());
    Assert.assertEquals(200, response.getStatus());
  }
}
