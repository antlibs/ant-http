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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.missinglink.tools.StreamUtils.inputStreamToByteArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntityNodeTest extends AbstractAntTest {

  public EntityNodeTest() throws IOException {
    super("<target name=\"value_put\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"PUT\" entityProperty=\"put_entity\">\n" +
        "    <entity value=\"${put_value}\" />\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"text_put\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"PUT\" entityProperty=\"put_entity\">\n" +
        "    <entity>${put_text}</entity>\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"file_put\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"PUT\" entityProperty=\"put_entity\">\n" +
        "    <entity file=\"${put_file}\" />\n" +
        "  </http>\n" +
        "</target>\n" +
        "<target name=\"binary_put\">\n" +
        "  <http url=\"${server_uri}${server_context}\" method=\"PUT\" outFile=\"${put_outfile}\">\n" +
        "    <entity file=\"${put_file}\" binary=\"true\" />\n" +
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
  public void testValuePut() {
    project.setProperty("server_context", ECHO_CONTEXT);
    project.setProperty("put_value", ECHO_TEXT);
    project.setProperty("put_entity", null);
    buildRule.executeTarget("value_put");

    assertEquals(ECHO_TEXT, project.getProperty("put_entity"));
  }

  @Test
  public void testTextPut() {
    project.setProperty("server_context", ECHO_CONTEXT);
    project.setProperty("put_text", ECHO_TEXT);
    project.setProperty("put_entity", null);
    buildRule.executeTarget("text_put");

    assertEquals(ECHO_TEXT, project.getProperty("put_entity"));
  }

  @Test
  public void testFilePut() throws IOException {
    final File inFile = File.createTempFile("test", ".in");
    inFile.deleteOnExit();
    final FileOutputStream inStream = new FileOutputStream(inFile);
    inStream.write(ECHO_TEXT.getBytes());
    inStream.close();

    project.setProperty("server_context", ECHO_CONTEXT);
    project.setProperty("put_file", inFile.getAbsolutePath());
    project.setProperty("put_entity", null);
    buildRule.executeTarget("file_put");

    assertEquals(ECHO_TEXT, project.getProperty("put_entity"));
  }

  @Test
  public void testBinaryPut() throws IOException {
    final File inFile = new File(getClass().getResource(HW_ZIP).getFile());
    final File outFile = File.createTempFile(HW_ZIP, ".out");
    outFile.deleteOnExit();

    project.setProperty("server_context", ECHO_CONTEXT);
    project.setProperty("put_file", inFile.getAbsolutePath());
    project.setProperty("put_outfile", outFile.getAbsolutePath());
    buildRule.executeTarget("binary_put");

    final FileInputStream inStream = new FileInputStream(inFile);
    final byte[] inBytes = inputStreamToByteArray(inStream);
    inStream.close();

    final FileInputStream outStream = new FileInputStream(outFile);
    final byte[] outBytes = inputStreamToByteArray(outStream);
    outStream.close();

    assertArrayEquals("Input and output data do not match", inBytes, outBytes);
  }

}
