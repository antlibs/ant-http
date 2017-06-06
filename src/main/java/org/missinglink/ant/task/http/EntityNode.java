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

package org.missinglink.ant.task.http;

import java.io.File;

/**
 * @author alex.sherwin
 *
 */
public class EntityNode {

  private String text;
  private File file;
  private String value;
  private boolean binary = false;

  public EntityNode() {
    super();
  }

  public File getFile() {
    return file;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public void addText(final String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setBinary(final boolean binary) {
    this.binary = binary;
  }

  public boolean getBinary() {
    return binary;
  }

  public boolean isValid() {
    return null != text && text.length() > 0 || null != file;
  }

}
