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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryNodeTest extends AbstractAntTest {

  public QueryNodeTest() throws IOException {
    super("<target name=\"query_get\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"GET\" printResponse=\"true\">\n" +
        "    <query>\n" +
        "      <parameter name=\"${query_param1}\" value=\"${query_value1}\" />\n" +
        "      <parameter name=\"${query_param2}\" value=\"${query_value2}\" />\n" +
        "    </query>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"query_post\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"POST\" printResponse=\"true\">\n" +
        "    <query>\n" +
        "      <parameter name=\"${query_param1}\" value=\"${query_value1}\" />\n" +
        "      <parameter name=\"${query_param2}\" value=\"${query_value2}\" />\n" +
        "    </query>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"query_get_uri\">\n" +
        "  <http url=\"${server_uri}${server_context}?${query_param1}=${query_value1}&amp;${query_param2}=${query_value2}\" method=\"GET\" printResponse=\"true\"/>\n" +
        "</target>\n" +
        "<target name=\"query_get_mixed\">\n" +
        "  <http url=\"${server_uri}${server_context}?${query_param1}=${query_value1}\" method=\"GET\" printResponse=\"true\">\n" +
        "    <query>\n" +
        "      <parameter name=\"${query_param2}\" value=\"${query_value2}\" />\n" +
        "    </query>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"query_post_mixed\">\n" +
        "  <http url=\"${server_uri}${server_context}?${query_param1}=${query_value1}\" method=\"POST\" printResponse=\"true\">\n" +
        "    <query>\n" +
        "      <parameter name=\"${query_param2}\" value=\"${query_value2}\" />\n" +
        "    </query>\n" +
        "  </http>\n" +
        "</target>");
  }

  @Before
  public void before() throws Exception {
    startHttpServer();
    project.setNewProperty("server_uri", getHttpServerUri());
    project.setProperty("server_context", ECHO_CONTEXT);
  }

  @After
  public void after() throws IOException {
    stopHttpServer();
  }

  @Test
  public void testQueryGet() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_get", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] foo")));
  }

  @Test
  public void testQueryGetComplex() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "SELECT Id,Email FROM Account WHERE Email = 'test@test.com'");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_get", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] SELECT Id,Email FROM Account WHERE Email = 'test@test.com'")));
  }

  @Test
  public void testQueryPost() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_post", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] foo")));
  }

  @Test
  public void testQueryPostComplex() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "SELECT Id,Email FROM Account WHERE Email = 'test@test.com'");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_post", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] SELECT Id,Email FROM Account WHERE Email = 'test@test.com'")));
  }

  @Test
  public void testQueryGetUri() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_get_uri", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] foo")));
  }

  @Test
  public void testQueryGetMixed2() {
    project.setProperty("query_param1", "bar");
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", ECHO_TEXT);
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_get_mixed", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] baz")));
  }

  @Test
  public void testQueryGetMixed1() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_get_mixed", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] foo")));
  }

  @Test
  public void testQueryPostMixed1() {
    project.setProperty("query_param1", ECHO_TEXT);
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", "bar");
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_post_mixed", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] foo")));
  }

  @Test
  public void testQueryPostMixed2() {
    project.setProperty("query_param1", "bar");
    project.setProperty("query_value1", "foo");
    project.setProperty("query_param2", ECHO_TEXT);
    project.setProperty("query_value2", "baz");
    final List<String> taskLog = buildRule.logExecuteTarget("query_post_mixed", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(equalTo("[http] baz")));
  }

}
