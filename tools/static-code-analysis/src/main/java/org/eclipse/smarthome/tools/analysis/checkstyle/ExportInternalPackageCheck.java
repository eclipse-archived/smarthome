/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.ivy.osgi.core.BundleInfo;
import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if a manifest file exports internal packages.
 *
 * @author Svilen Valkanov
 *
 */
public class ExportInternalPackageCheck extends AbstractStaticCheck {

  public final static String MANIFEST_EXTENSTION = "MF";
  public final static String MESSAGE_INTERNAL_PACKAGE_EXPORTED = "Remove internal package export";
  public final static String MESSAGE_FILE_EMPTY = "File is empty !";

  public ExportInternalPackageCheck() {
    setFileExtensions(MANIFEST_EXTENSTION);
  }

  @Override
  protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
    if (isEmpty(file)) {
      log(0, MESSAGE_FILE_EMPTY, new Integer(0));
      return;
    }
    BundleInfo manifest = parseManifestFromFile(file);
    Set<?> exports = manifest.getExports();

    int lineNumber = 0;
    for (Object export : exports) {
      String pacakgeName = export.toString();
      if (pacakgeName.contains(".internal")) {
        lineNumber = findLineNumber(lines, pacakgeName, lineNumber);
        log(lineNumber, MESSAGE_INTERNAL_PACKAGE_EXPORTED + " " + pacakgeName, new Integer(0));
      }
    }

  }
}
