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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.BuildListener;

public class LogTargetBuildFileRule extends BuildFileRule {

  public List<String> logExecuteTarget(final String targetName, final int logLevel) {

    final List<String> taskLog = new ArrayList<String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String logEntry : this) {
          sb.append(logEntry);
          sb.append("\n");
        }
        return sb.toString();
      }
    };

    final BuildListener listener = new BuildListener() {
      @Override
      public void buildStarted(final BuildEvent event) {
      }

      @Override
      public void buildFinished(final BuildEvent event) {
      }

      @Override
      public void targetStarted(final BuildEvent event) {
      }

      @Override
      public void targetFinished(final BuildEvent event) {
      }

      @Override
      public void taskStarted(final BuildEvent event) {
      }

      @Override
      public void taskFinished(final BuildEvent event) {
      }

      @Override
      public void messageLogged(final BuildEvent event) {
        if (event.getPriority() > logLevel) {
          return;
        }
        if (null == event.getTarget() || !targetName.equals(event.getTarget().getName())) {
          return;
        }
        taskLog.add(String.format("[%s] %s", event.getTask().getTaskName(), event.getMessage()));
      }
    };

    getProject().addBuildListener(listener);
    try {
      super.executeTarget(targetName);
    } finally {
      getProject().removeBuildListener(listener);
    }

    return taskLog;
  }

}
