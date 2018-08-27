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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Rule;
import org.missinglink.http.server.AbstractHttpServerTest;

public abstract class AbstractAntTest extends AbstractHttpServerTest {

  @Rule
  public final LogTargetBuildFileRule buildRule = new LogTargetBuildFileRule();
  protected Project project;
  private final String buildfile;

  public AbstractAntTest(final String tasksxml) throws IOException {

    super();

    final File temp = File.createTempFile("build", ".xml");
    temp.deleteOnExit();
    final FileOutputStream fos = new FileOutputStream(temp);
    fos.write(("<?xml version=\"1.1\" encoding=\"UTF-8\"?>\n" +
        "<project>\n" +
        "  <taskdef name=\"http\" classname=\"org.missinglink.ant.task.http.HttpClientTask\" />\n" +
        tasksxml +
        "</project>").getBytes());
    fos.close();

    this.buildfile = temp.getAbsolutePath();

  }

  @Before
  public void beforeBuildfile() {

    buildRule.configureProject(buildfile);
    project = buildRule.getProject();

  }

}
