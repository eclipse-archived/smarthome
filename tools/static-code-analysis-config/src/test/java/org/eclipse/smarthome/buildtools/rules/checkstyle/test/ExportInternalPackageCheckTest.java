/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.buildtools.rules.checkstyle.test;

import java.io.File;
import java.io.IOException;

import org.eclipse.smarthome.buildtools.rules.checkstyle.ExportInternalPackageCheck;
import org.junit.Test;

import com.google.checkstyle.test.base.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Tests for {@link ExportInternalPackageCheck}
 * 
 * @author Svilen Valkanov
 *
 */
public class ExportInternalPackageCheckTest extends BaseCheckTestSupport {
    @Override
    protected String getPath(String fileName) throws IOException {
        return new File(
                "src/test/resources/org/eclipse/smarthome/buildtools/rules/checkstyle/test" + File.separator + fileName)
                        .getCanonicalPath();
    }

    @Test
    public void testManifestFileThatExportsInternalPackage() throws Exception {
        DefaultConfiguration config = createCheckConfig(ExportInternalPackageCheck.class);
        String testFileName = "MANIFEST.MF";
        int lineNumber = 12;

        String[] expectedMessages = { lineNumber + ": " + ExportInternalPackageCheck.MSG_KEY };

        String filePath = getPath(testFileName);

        Integer[] warnList = { lineNumber };

        verify(config, filePath, expectedMessages, warnList);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration dc = new DefaultConfiguration("root");
        dc.addChild(config);
        return dc;
    }

}
