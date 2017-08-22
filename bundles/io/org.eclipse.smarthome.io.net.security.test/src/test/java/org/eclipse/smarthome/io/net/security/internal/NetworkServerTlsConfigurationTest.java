/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link ServiceConfiguration} class, especially the Base64 encoded passwords
 * and multi-context entries.
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkServerTlsConfigurationTest {
    private ServiceConfiguration config;
    private File defaultCfgFile;
    private File workingDir;
    private ComputeConfigurationDifference utils;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        workingDir = new File(Paths.get("").toAbsolutePath().toString());
        defaultCfgFile = new File(workingDir, "testconfig.keystore.cfg");

        utils = new ComputeConfigurationDifference(null, new ArrayList<>());
        config = new ServiceConfiguration();

        if (defaultCfgFile.exists()) {
            defaultCfgFile.delete();
        }
    }

    @Test
    public void notExistingIsDefaultContext() throws IOException {
        assert config != null;
        ContextConfiguration second = new ContextConfiguration();
        second.context = "second";
        config.contexts.add(second);
        assertThat(config.contexts.size(), is(1));
        assertThat(utils.getOrCreateContext(config, "default"), is(utils.getOrCreateContext(config, "something")));
        assertThat(utils.getOrCreateContext(config, "default"), is(utils.getOrCreateContext(config, null)));
        assertThat(config.contexts.size(), is(2));
    }
}
