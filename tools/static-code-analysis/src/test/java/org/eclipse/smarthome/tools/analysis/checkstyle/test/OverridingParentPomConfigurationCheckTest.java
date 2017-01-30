/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle.test;

import java.io.File;

import org.eclipse.smarthome.tools.analysis.checkstyle.OverridingParentPomConfigurationCheck;
import org.eclipse.smarthome.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.junit.BeforeClass;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link OverridingParentPomConfigurationCheck}
 *
 * @author Aleksandar Kovachev
 *
 */
public class OverridingParentPomConfigurationCheckTest extends AbstractStaticCheckTest {

    private static final String FILE_NAME = File.separator + OverridingParentPomConfigurationCheck.FILE_NAME;
    private static final String TEST_DIRECTORY = "overridingParentPomConfigurationCheck" + File.separator;

    static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createCheckConfig(OverridingParentPomConfigurationCheck.class);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void testInvalidPomConfiguration() throws Exception {
        int lineNumber = 9;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                OverridingParentPomConfigurationCheck.MESSAGE_OVERRIDING_POM_CONFIGURATION_FOUND);
        verify(config, getPath(TEST_DIRECTORY + "invalidPomConfiguration" + FILE_NAME), expectedMessages);
    }

    @Test
    public void testMissingOverridingParentPomConfiguration() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verify(config, getPath(TEST_DIRECTORY + "missingOverridingParentPomConfiguration" + FILE_NAME),
                expectedMessages);
    }

    @Test
    public void testEmptyPom() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                OverridingParentPomConfigurationCheck.EMPTY_FILE_MESSAGE);
        verify(config, getPath(TEST_DIRECTORY + "emptyPom" + FILE_NAME), expectedMessages);
    }
}
