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

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public class HttpsTrustAllTaskTest extends AbstractHttpTaskTest {

  public HttpsTrustAllTaskTest() throws IOException {
    super("<target name=\"simple_get\">\n" +
        "  <http url=\"${server_uri}${server_context}\">\n" +
        "    <keystore trustall=\"true\" />\n" +
        "  </http>\n" +
        "</target>");
  }

  @Before
  public void beforeTask() throws Exception {
    startHttpsServer();
    project.setNewProperty("server_uri", getHttpsServerUri());
  }

  @After
  public void afterTask() {
    stopHttpsServer();
  }

}
