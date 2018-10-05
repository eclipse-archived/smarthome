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
package org.eclipse.smarthome.storage.json.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.smarthome.core.storage.Storage;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Henning Treu - inital contribution
 *
 */
public class JsonStorageServiceTest {

    private static final String STORAGE_NAME = "myStorage";
    private JsonStorageService jsonStorageService;

    @Before
    public void setup() {
        jsonStorageService = new JsonStorageService();
    }

    @Test
    public void getSameStorageForSameClassLoader() {
        ClassLoader classLoader1 = mock(ClassLoader.class);

        Storage<Object> storage = jsonStorageService.getStorage(STORAGE_NAME, classLoader1);

        assertThat(storage, is(jsonStorageService.getStorage(STORAGE_NAME, classLoader1)));
    }

    @Test
    public void getNewStorageForDifferentClassLoader() {
        ClassLoader classLoader1 = mock(ClassLoader.class);
        ClassLoader classLoader2 = mock(ClassLoader.class);

        Storage<Object> storage = jsonStorageService.getStorage(STORAGE_NAME, classLoader1);

        assertThat(storage, is(not(jsonStorageService.getStorage(STORAGE_NAME, classLoader2))));
    }

}
