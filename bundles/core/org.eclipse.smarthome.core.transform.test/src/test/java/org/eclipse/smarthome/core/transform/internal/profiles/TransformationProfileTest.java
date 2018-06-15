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
package org.eclipse.smarthome.core.transform.internal.profiles;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class TransformationProfileTest {

    private TransformationProfile profile;
    private TransformationService service;
    private ProfileContext context;
    private ProfileTypeUID typeUID;

    @Before
    public void setup() throws TransformationException {
        service = mock(TransformationService.class);
        when(service.transform(any(), any())).thenReturn("TransformedValue");

        context = mock(ProfileContext.class);
        Configuration config = new Configuration();
        config.put("function", "DUMMY");
        config.put("sourceFormat", "%s");
        when(context.getConfiguration()).thenReturn(config);

        typeUID = new ProfileTypeUID("transform", "DUMMY");
    }

    @Test
    public void testTransformStringType() {

        ProfileCallback callback = mock(ProfileCallback.class);

        profile = new TransformationProfile(callback, context, service, typeUID);

        Command cmd = new StringType("Hello");

        profile.onCommandFromHandler(cmd);
        ArgumentCaptor<Command> captur = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(captur.capture());
        assertEquals("TransformedValue", captur.getValue().toFullString());

        callback = mock(ProfileCallback.class);
        profile = new TransformationProfile(callback, context, service, typeUID);
        profile.onStateUpdateFromHandler((State) cmd);
        ArgumentCaptor<State> capturState = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capturState.capture());
        assertEquals("TransformedValue", capturState.getValue().toFullString());

        callback = mock(ProfileCallback.class);
        profile = new TransformationProfile(callback, context, service, typeUID);
        profile.onCommandFromItem(cmd);
        captur = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(captur.capture());
        assertEquals("Hello", captur.getValue().toFullString());

        callback = mock(ProfileCallback.class);
        profile = new TransformationProfile(callback, context, service, typeUID);
        profile.onStateUpdateFromItem((State) cmd);
        captur = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(captur.capture());
        assertEquals("Hello", captur.getValue().toFullString());
    }

    @Test
    public void testTransformDecimalType() {

        ProfileCallback callback = mock(ProfileCallback.class);

        profile = new TransformationProfile(callback, context, service, typeUID);

        Command cmd = new DecimalType(23);
        profile.onCommandFromHandler(cmd);

        ArgumentCaptor<Command> captur = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(captur.capture());

        assertTrue(captur.getValue() instanceof StringType);
        assertEquals("TransformedValue", captur.getValue().toFullString());
    }

    @Test
    public void testTransformException() throws TransformationException {

        ProfileCallback callback = mock(ProfileCallback.class);

        doThrow(new TransformationException("Error occured")).when(service).transform(any(), any());

        profile = new TransformationProfile(callback, context, service, typeUID);

        Command cmd = new StringType("Hello World");
        profile.onCommandFromHandler(cmd);

        ArgumentCaptor<Command> captur = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(captur.capture());

        // we expect the original input string as output if the transformation fails
        assertEquals("Hello World", captur.getValue().toFullString());
    }

}
