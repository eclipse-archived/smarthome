/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class DefaultSlaveProfileTest {

    @Mock
    private ProfileCallback mockCallback;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testOnCommand() {
        DefaultSlaveProfile profile = new DefaultSlaveProfile(mockCallback);

        profile.onCommand(OnOffType.ON);

        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void testOnUpdate() {
        DefaultSlaveProfile profile = new DefaultSlaveProfile(mockCallback);
        profile.onUpdate(OnOffType.ON);

        verify(mockCallback).handleCommand(eq(OnOffType.ON));
        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void testStateUpdated() {
        DefaultSlaveProfile profile = new DefaultSlaveProfile(mockCallback);
        profile.stateUpdated(OnOffType.ON);

        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void testPostCommand() {
        DefaultSlaveProfile profile = new DefaultSlaveProfile(mockCallback);
        profile.postCommand(OnOffType.ON);

        verify(mockCallback).sendCommandEvent(eq(OnOffType.ON));
        verifyNoMoreInteractions(mockCallback);
    }

}
