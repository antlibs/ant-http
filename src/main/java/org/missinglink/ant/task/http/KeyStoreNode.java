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

import org.apache.tools.ant.types.DataType;

/**
 * @author alex.sherwin
 *
 */
public class KeyStoreNode extends DataType {

  private File file;
  private String password;
  private boolean trustAll = false;

  public KeyStoreNode() {
    super();
  }

  public boolean getTrustAll() {
    return trustAll;
  }

  public void setTrustAll(final boolean trustall) {
    trustAll = trustall;
  }

  public File getFile() {
    return file;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public boolean isValid() {
    return null != file || trustAll == true;
  }

}
