/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.id;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.junit.Test;

/**
 * @author Kai Kreuzer - Initial contribution
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class UUIDTest {

    @Test
    public void sameUUID() {
        String uuid1 = InstanceUUID.get();
        String uuid2 = InstanceUUID.get();
        assertThat(uuid1, is(equalTo(uuid2)));
    }

    @Test
    public void readFromPersistedFile() throws FileNotFoundException, IOException {
        // we first need to remove the cached value
        InstanceUUID.uuid = null;
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + InstanceUUID.UUID_FILE_NAME);
        file.getParentFile().mkdirs();
        try (OutputStream os = new FileOutputStream(file)) {
            IOUtils.write("123", os);
        }
        String uuid = InstanceUUID.get();
        assertThat(uuid, is(equalTo("123")));
    }

    @Test
    public void ignoreEmptyFile() throws FileNotFoundException, IOException {
        // we first need to remove the cached value
        InstanceUUID.uuid = null;
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + InstanceUUID.UUID_FILE_NAME);
        file.getParentFile().mkdirs();
        try (OutputStream os = new FileOutputStream(file)) {
            IOUtils.write("", os);
        }
        String uuid = InstanceUUID.get();
        assertThat(uuid, not(equalTo("")));
    }
}
