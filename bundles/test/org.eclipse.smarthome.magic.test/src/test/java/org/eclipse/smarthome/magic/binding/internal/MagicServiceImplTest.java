/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.magic.binding.internal;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Collection;

import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.magic.binding.MagicService;
import org.junit.Before;
import org.junit.Test;

public class MagicServiceImplTest {

    private static final String PARAMETER_NAME = "select_decimal_limit";

    private MagicService magicService;

    @Before
    public void setup() {
        magicService = new MagicServiceImpl();
    }

    @Test
    public void shouldProvideConfigOptionsForURIAndParameterName() {
        Collection<ParameterOption> parameterOptions = magicService.getParameterOptions(MagicService.CONFIG_URI,
                PARAMETER_NAME, null);

        assertThat(parameterOptions, hasSize(3));
    }

    @Test
    public void shouldProvidemtpyListForInvalidURI() {
        Collection<ParameterOption> parameterOptions = magicService.getParameterOptions(URI.create("system.audio"),
                PARAMETER_NAME, null);

        assertNull(parameterOptions);
    }

    @Test
    public void shouldProvidemtpyListForInvalidParameterName() {
        Collection<ParameterOption> parameterOptions = magicService.getParameterOptions(MagicService.CONFIG_URI,
                "some_param_name", null);

        assertNull(parameterOptions);
    }
}
