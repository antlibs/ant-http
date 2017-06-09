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

package org.missinglink.ant.task.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.Test;

public abstract class AbstractHttpTaskTest extends AbstractAntTest {

  public AbstractHttpTaskTest(final String tasksxml) throws IOException {
    super(tasksxml);
  }

  @Test
  public void testSimpleGet200() {
    project.setProperty("server_context", PING_CONTEXT);
    final List<String> taskLog = buildRule.logExecuteTarget("simple_get", Project.MSG_DEBUG);

    assertThat(taskLog, hasItem(startsWith("[http] HTTP GET ")));
    assertThat(taskLog, hasItem(equalTo("[http] Status:\t\t200")));
  }

  @Test
  public void testSimpleGet404() {
    project.setProperty("server_context", "/doesnt/exist");
    try {
      buildRule.executeTarget("simple_get");
      fail("Target should have thrown a BuildException");
    } catch (final BuildException ex) {
      assertThat(ex.getMessage(), startsWith("Expected Status [200] but got [404] for URI"));
    }
  }

  @Test
  public void testSimpleGet500() {
    project.setProperty("server_context", INTERNAL_SERVER_ERROR_CONTEXT);
    try {
      buildRule.executeTarget("simple_get");
      fail("Target should have thrown a BuildException");
    } catch (final BuildException ex) {
      assertThat(ex.getMessage(), startsWith("Expected Status [200] but got [500] for URI"));
    }
  }

}
