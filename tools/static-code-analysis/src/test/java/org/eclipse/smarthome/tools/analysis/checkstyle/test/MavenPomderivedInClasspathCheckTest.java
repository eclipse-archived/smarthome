/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle.test;

import java.io.File;

import org.eclipse.smarthome.tools.analysis.checkstyle.MavenPomderivedInClasspathCheck;
import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.junit.BeforeClass;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link MavenPomderivedInClasspathCheck}
 *
 * @author Aleksandar Kovachev
 *
 */
public class MavenPomderivedInClasspathCheckTest extends AbstractStaticCheckTest {

    private static final String FILE_NAME = File.separator + "." + MavenPomderivedInClasspathCheck.CLASSPATH_NAME;
    private static final String TEST_DIRECTORY = "mavenPomDerivedInClasspathCheck" + File.separator;

    static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createCheckConfig(MavenPomderivedInClasspathCheck.class);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void testEmptyClassPath() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                MavenPomderivedInClasspathCheck.EMPTY_FILE_MESSAGE);
        verify(config, getPath(TEST_DIRECTORY + "emptyClasspath" + FILE_NAME), expectedMessages);
    }

    @Test
    public void testValidClasspathConfigurationTest() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verify(config, getPath(TEST_DIRECTORY + "validClasspathConfiguration" + FILE_NAME), expectedMessages);
    }

    @Test
    public void testMissingPomderivedAttributeInClassPath() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verify(config, getPath(TEST_DIRECTORY + "missingPomderivedAttributeInClassPath" + FILE_NAME), expectedMessages);
    }

    @Test
    public void testInvalidClasspathConfiguration() throws Exception {
        int lineNumber = 7;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                MavenPomderivedInClasspathCheck.MESSAGE_POMDERIVED_FOUND);
        verify(config, getPath(TEST_DIRECTORY + "invalidClasspathConfiguration" + FILE_NAME), expectedMessages);
    }
}
