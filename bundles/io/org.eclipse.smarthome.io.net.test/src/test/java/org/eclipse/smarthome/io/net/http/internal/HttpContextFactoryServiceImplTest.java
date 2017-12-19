/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.net.http.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.io.net.http.HttpContextFactoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public class HttpContextFactoryServiceImplTest {

    private static final String RESOURCE = "resource";

    private HttpContextFactoryService httpContextFactoryService;

    @Mock
    private Bundle bundle;

    @Before
    public void setup() {
        initMocks(this);
        httpContextFactoryService = new HttpContextFactoryServiceImpl();
    }

    @Test
    public void shouldCreateHttpContext() {
        HttpContext context = httpContextFactoryService.createDefaultHttpContext(bundle);
        assertThat(context, is(notNullValue()));
    }

    @Test
    public void httpContextShouldCallgetResourceOnBundle() {
        HttpContext context = httpContextFactoryService.createDefaultHttpContext(bundle);
        context.getResource(RESOURCE);

        verify(bundle).getResource(RESOURCE);
    }

    @Test
    public void httpContextShouldCallgetResourceOnBundleWithoutLeadingSlash() {
        HttpContext context = httpContextFactoryService.createDefaultHttpContext(bundle);
        context.getResource("/" + RESOURCE);

        verify(bundle).getResource(RESOURCE);
    }
}
