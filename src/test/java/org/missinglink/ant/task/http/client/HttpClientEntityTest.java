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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.missinglink.http.client.HttpClient;
import org.missinglink.http.exception.InvalidStreamException;
import org.missinglink.http.exception.InvalidUriException;

/**
 * @author alex.sherwin
 *
 */
public class HttpClientEntityTest {

  public HttpClientEntityTest() {
    super();
  }

  @Test
  public void testNullInputStreamEntity() throws InvalidUriException, InvalidStreamException {
    final InputStream is = null;
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(is).toHttpClient();
    Assert.assertNull(httpClient.getEntity());
  }

  @Test
  public void testNullStringEntity() throws InvalidUriException {
    final String str = null;
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(str).toHttpClient();
    Assert.assertNull(httpClient.getEntity());
  }

  @Test
  public void testInputStreamEntity() throws InvalidUriException, IOException, InvalidStreamException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(new ByteArrayInputStream(str.getBytes())).toHttpClient();
    Assert.assertEquals(str.getBytes().length, httpClient.getEntity().available());
  }

  @Test
  public void testStringEntity() throws InvalidUriException, IOException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(str).toHttpClient();
    Assert.assertEquals(str.getBytes().length, httpClient.getEntity().available());
  }

  @Test
  public void testInputStreamEntityToString() throws InvalidUriException, IOException, InvalidStreamException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(new ByteArrayInputStream(str.getBytes())).toHttpClient();
    Assert.assertEquals(str, httpClient.getEntityAsString());
  }

  @Test
  public void testStringEntityToString() throws InvalidUriException, IOException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(str).toHttpClient();
    Assert.assertEquals(str, httpClient.getEntityAsString());
  }

  @Test
  public void testInputStreamEntityToStringMultipleReads() throws InvalidUriException, IOException, InvalidStreamException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(new ByteArrayInputStream(str.getBytes())).toHttpClient();
    Assert.assertEquals(str, httpClient.getEntityAsString());
    Assert.assertEquals(str, httpClient.getEntityAsString());
  }

  @Test
  public void testStringEntityToStringMultipleReads() throws InvalidUriException, IOException {
    final String str = "Hello World";
    final HttpClient httpClient = HttpClient.uri("http://host/context").entity(str).toHttpClient();
    Assert.assertEquals(str, httpClient.getEntityAsString());
    Assert.assertEquals(str, httpClient.getEntityAsString());
  }

}
