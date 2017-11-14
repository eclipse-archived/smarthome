/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.id

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.apache.commons.io.IOUtils
import org.eclipse.smarthome.config.core.ConfigConstants
import org.junit.Test

class UUIDTest {

    @Test
    void 'same UUID'() {
        def uuid1 = InstanceUUID.get()
        def uuid2 = InstanceUUID.get()
        assertThat uuid1, is(equalTo(uuid2))
    }

    @Test
    void 'read from persisted file'() {
        // we first need to remove the cached value
        InstanceUUID.uuid = null
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + InstanceUUID.UUID_FILE_NAME);
        file.getParentFile().mkdirs();
        IOUtils.write("123", new FileOutputStream(file))
        def uuid = InstanceUUID.get()
        assertThat uuid, is(equalTo("123"))
    }

    @Test
    void 'ignore empty file'() {
        // we first need to remove the cached value
        InstanceUUID.uuid = null
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + InstanceUUID.UUID_FILE_NAME);
        file.getParentFile().mkdirs();
        IOUtils.write("", new FileOutputStream(file))
        def uuid = InstanceUUID.get()
        assertThat uuid, not(equalTo(""))
    }
}
