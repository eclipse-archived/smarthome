/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.buildtools.report.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.eclipse.smarthome.buildtools.report.ScanReportUtility;
import org.junit.Test;

/**
 * Tests for the {@link ScanReportUtility}
 * 
 * @author Svilen Valkanov
 */

public class ScanReportUtilityTest {

    public static final String TARGET_RELATIVE_DIR = "target" + File.separator + "test-classes";
    public static final String TARGET_ABSOLUTE_DIR = System.getProperty("user.dir") + File.separator
            + TARGET_RELATIVE_DIR;
    public static final String RESULT_FILE_PATH = TARGET_ABSOLUTE_DIR + File.separator
            + ScanReportUtility.RESULT_FILE_NAME;

    @Test
    public void assertReportIsCreated() {
        File file = new File(RESULT_FILE_PATH);

        if (file.exists()) {
            file.delete();
        }

        assertFalse(file.exists());

        String[] args = { TARGET_ABSOLUTE_DIR };
        ScanReportUtility.main(args);

        assertTrue(file.exists());
    }

}
