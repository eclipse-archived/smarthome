/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle.test;

import java.io.File;

import org.eclipse.smarthome.tools.analysis.checkstyle.JavadocFilterCheck;
import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocStyleCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link JavadocInnerClassesFilterCheck}
 * 
 * @author Petar Valchev
 *
 */
public class JavadocFilterCheckTest extends AbstractStaticCheckTest {
    @Test
    public void testOuterClassWithJavadoc() throws Exception {
        int[] lineNumbers = new int[] { 10 };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", false, lineNumbers);
    }

    @Test
    public void testOuterAndInnerClassesWithJavadoc() throws Exception {
        int[] lineNumbers = new int[] { 10, 12 };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", true, lineNumbers);
    }

    @Test
    public void testOuterClassWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", false, null);
    }

    @Test
    public void testOuterAndInnerClassesWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", true, null);
    }

    private void verifyJavadoc(String testFileName, boolean checkInnerClasses, int[] lineNumbers) throws Exception {
        DefaultConfiguration config = createCheckConfig(JavadocFilterCheck.class);
        String checkInnerClassesPropertyName = "checkInnerUnits";
        config.addAttribute(checkInnerClassesPropertyName, String.valueOf(checkInnerClasses));

        String testDirectory = "javadocFilterCheckTest";
        String filePath = getPath(testDirectory + File.separator + testFileName);

        String[] expectedMessages = null;
        if (lineNumbers != null) {
            expectedMessages = new String[lineNumbers.length];
            for (int i = 0; i < lineNumbers.length; i++) {
                expectedMessages[i] = lineNumbers[i] + ": " + JavadocStyleCheck.MSG_JAVADOC_MISSING;
            }
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verify(config, filePath, expectedMessages);
    }
}
