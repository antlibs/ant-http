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

package org.missinglink.ant.task.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HeaderNodeTest extends AbstractAntTest {

  public HeaderNodeTest() throws IOException {
    super("<target name=\"headers_get\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"GET\">\n" +
        "    <headers>\n" +
        "      <header name=\"${header_name1}\" value=\"${header_value1}\" />\n" +
        "      <header name=\"${header_name2}\" value=\"${header_value2}\" />\n" +
        "    </headers>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"silent_request_headers\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"GET\" printRequestHeaders=\"false\">\n" +
        "    <headers>\n" +
        "      <header name=\"${header_name1}\" value=\"${header_value1}\" />\n" +
        "      <header name=\"${header_name2}\" value=\"${header_value2}\" />\n" +
        "    </headers>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"silent_response_headers\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"GET\" printResponseHeaders=\"false\">\n" +
        "    <headers>\n" +
        "      <header name=\"${header_name1}\" value=\"${header_value1}\" />\n" +
        "      <header name=\"${header_name2}\" value=\"${header_value2}\" />\n" +
        "    </headers>\n" +
        "  </http>\n" +
        "</target>");
  }

  @Before
  public void before() throws Exception {
    startHttpServer();
    project.setNewProperty("server_uri", getHttpServerUri());
  }

  @After
  public void after() {
    stopHttpServer();
  }

  @Test
  public void testHeadersGet() {
    project.setProperty("server_context", ECHO_HEADERS_CONTEXT);
    project.setProperty("header_name1", "Content-Type");
    project.setProperty("header_value1", "text/xml");
    project.setProperty("header_name2", "Accept");
    project.setProperty("header_value2", "text/plain, image/png");
    final List<String> taskLog = buildRule.logExecuteTarget("headers_get", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] \tContent-Type: text/xml")));
    assertThat(taskLog, hasItem(equalTo("[http] \tAccept: text/plain, image/png")));
    assertThat(taskLog, hasItem(equalTo("[http] \tX-Req-Content-type: text/xml")));
    assertThat(taskLog, hasItem(equalTo("[http] \tX-Req-Accept: text/plain, image/png")));
  }

  @Test
  public void testSilentRequestHeaders() {
    project.setProperty("server_context", ECHO_HEADERS_CONTEXT);
    project.setProperty("header_name1", "Content-Type");
    project.setProperty("header_value1", "text/xml");
    project.setProperty("header_name2", "Accept");
    project.setProperty("header_value2", "text/plain, image/png");
    final List<String> taskLog = buildRule.logExecuteTarget("silent_request_headers", Project.MSG_DEBUG);

    assertThat(taskLog, not(hasItem(equalTo("[http] \tContent-Type: text/xml"))));
    assertThat(taskLog, not(hasItem(equalTo("[http] \tAccept: text/plain, image/png"))));
    assertThat(taskLog, hasItem(equalTo("[http] \tX-Req-Content-type: text/xml")));
    assertThat(taskLog, hasItem(equalTo("[http] \tX-Req-Accept: text/plain, image/png")));
  }

  @Test
  public void testSilentResponseHeaders() {
    project.setProperty("server_context", ECHO_HEADERS_CONTEXT);
    project.setProperty("header_name1", "Content-Type");
    project.setProperty("header_value1", "text/xml");
    project.setProperty("header_name2", "Accept");
    project.setProperty("header_value2", "text/plain, image/png");
    final List<String> taskLog = buildRule.logExecuteTarget("silent_response_headers", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] \tContent-Type: text/xml")));
    assertThat(taskLog, hasItem(equalTo("[http] \tAccept: text/plain, image/png")));
    assertThat(taskLog, not(hasItem(equalTo("[http] \tX-Req-Content-type: text/xml"))));
    assertThat(taskLog, not(hasItem(equalTo("[http] \tX-Req-Accept: text/plain, image/png"))));
  }
}
