/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Base test class for static code analysis checks
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Petar Valchev - Implement method to get the path in the expected format from checkstyle
 */
public abstract class AbstractStaticCheckTest extends BaseCheckTestSupport {

  /**
   * Resolves absolute path to a resource
   *
   * @param relativePathToFile - relative path to src/test/resources/checks/checkstyle . It should
   *          be "/" separated path.
   * @return absolute path or null if the string can not be parsed as URI
   */
  @Override
  protected String getPath(String relativePathToFile) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      String pathRelativeToResourcesDir = "checks/checkstyle/" + relativePathToFile;
      URL url = classLoader.getResource(pathRelativeToResourcesDir);
      URI uri = url.toURI();
      File file = new File(uri);
      return file.getCanonicalPath();
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Generates message that can be used in the some of the
   * {@link #verify(Configuration, String, String[], Integer...)} methods.
   *
   * @param arguments - a set of line number and message pairs
   * @return String[] in the format used from checkstyle to verify the logged messages
   */
  protected String[] generateExpectedMessages(Object... arguments) {
    int messageNumber = arguments.length / 2;
    String[] messages = new String[messageNumber];

    for (int i = 0; i < messageNumber; i++) {
      Object lineNum = arguments[2 * i];
      Object message = arguments[2 * i + 1];
      messages[i] = lineNum + ": " + message;
    }
    return messages;
  }
}
