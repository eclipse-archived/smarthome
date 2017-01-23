/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle.test;

import org.eclipse.smarthome.tools.analysis.checkstyle.ExportInternalPackageCheck;
import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link ExportInternalPackageCheck}
 *
 * @author Svilen Valkanov
 *
 */
public class ExportInternalPackageCheckTest extends AbstractStaticCheckTest {

  private final String testDirectory = "exportInternalPackageCheckTest";

  @Test
  public void testManifestFileExportsSingleInternalPackage() throws Exception {
    DefaultConfiguration config = createCheckConfig(ExportInternalPackageCheck.class);

    String testFileName = "singleInternalPackageExported.MF";
    String testFilePath = testDirectory + "/" + testFileName;

    String internalPackageName = "org.eclipse.smarthome.buildtools.internal";
    int lineNumber = 12;

    String[] expectedMessages = generateExpectedMessages(lineNumber,
        ExportInternalPackageCheck.MESSAGE_INTERNAL_PACKAGE_EXPORTED + " " + internalPackageName);

    String filePath = getPath(testFilePath);

    verify(config, filePath, expectedMessages);
  }

  @Test
  public void testManifestFileExportsMultipleInternalPackages() throws Exception {
    DefaultConfiguration config = createCheckConfig(ExportInternalPackageCheck.class);

    String testFileName = "multipleInternalPackagesExported.MF";
    String testFilePath = testDirectory + "/" + testFileName;

    String firstInternalPackageName = "org.eclipse.smarthome.buildtools.internal";
    int firstLineNumber = 12;
    String secondInternalPackageName = "org.eclipse.smarthome.buildtools.internal.test";
    int secondLineNumber = 13;

    String[] expectedMessages = generateExpectedMessages(firstLineNumber,
        ExportInternalPackageCheck.MESSAGE_INTERNAL_PACKAGE_EXPORTED + " "
            + firstInternalPackageName,
        secondLineNumber, ExportInternalPackageCheck.MESSAGE_INTERNAL_PACKAGE_EXPORTED + " "
            + secondInternalPackageName);

    String filePath = getPath(testFilePath);

    verify(config, filePath, expectedMessages);
  }

  @Test
  public void testEmptyFile() throws Exception {
    DefaultConfiguration config = createCheckConfig(ExportInternalPackageCheck.class);

    String emptyFileName = "emptyManifest.MF";
    String testFilePath = testDirectory + "/" + emptyFileName;
    int lineNumber = 0;

    String[] expectedMessages = generateExpectedMessages(lineNumber,
        ExportInternalPackageCheck.MESSAGE_FILE_EMPTY);

    String filePath = getPath(testFilePath);

    verify(config, filePath, expectedMessages);
  }

  @Test
  public void noInternalPackageExported() throws Exception {
    DefaultConfiguration config = createCheckConfig(ExportInternalPackageCheck.class);

    String emptyFileName = "noInternalPackageExported.MF";
    String testFilePath = testDirectory + "/" + emptyFileName;

    String[] expectedMessages = {};
    Integer[] warnList = {};

    String filePath = getPath(testFilePath);

    verify(config, filePath, expectedMessages);
  }

}
