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

import org.eclipse.smarthome.buildtools.rules.checkstyle.MethodLimitCheck;
import org.junit.Test;

import com.google.checkstyle.test.base.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link MethodLimitCheck}
 *
 * @author Svilen Valkanov
 *
 */

public class MethodLimitCheckTest extends BaseCheckTestSupport {

    @Override
    protected String getPath(String fileName) throws IOException {
        return new File(
                "src/test/resources/org/eclipse/smarthome/buildtools/rules/checkstyle/test" + File.separator + fileName)
                        .getCanonicalPath();
    }

    @Test
    public void testClassThatExceedsMethodLimit() throws Exception {
        DefaultConfiguration config = createCheckConfig(MethodLimitCheck.class);
        // Set the maximal number of methods to 1
        config.addAttribute("max", "1");
        String fileName = "MethodLimitCheckTestFile.java";
        int lineNumber = 3;

        String[] expected = { lineNumber + ": " + MethodLimitCheck.MSG_KEY };

        String filePath = getPath(fileName);

        Integer[] warnList = getLinesWithWarn(filePath);

        verify(config, filePath, expected, warnList);
    }

    @Test
    public void testClassThatDoesNotExceedMethodLimit() throws Exception {
        DefaultConfiguration config = createCheckConfig(MethodLimitCheck.class);
        // Set the maximal number of methods to 10
        config.addAttribute("max", "10");
        String fileName = "MethodLimitCheckTestFile.java";

        String[] expected = {};

        String filePath = getPath(fileName);

        verify(config, filePath, expected);
    }
}
