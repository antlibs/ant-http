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

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex.sherwin
 *
 */
public class HeadersNode {

  protected final List<HeaderNode> headers = new ArrayList<HeaderNode>();

  public HeadersNode() {
    super();
  }

  public void addConfiguredHeader(final HeaderNode header) {
    headers.add(header);
  }

  public List<HeaderNode> getHeaders() {
    return headers;
  }

  public boolean isValid() {
    return headers.size() > 0;
  }

}
