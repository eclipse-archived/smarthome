/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class RawButtonToggleProfileTest {

    @NonNull
    private static final String TEST_ITEM = "testItem";

    @NonNull
    private static final ThingUID THING_UID = new ThingUID("test", "thing");

    @NonNull
    private static final ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, "channel");

    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testSwitchItem() {
        TriggerProfile profile = new RawButtonToggleProfile();
        SwitchItem item = new SwitchItem(TEST_ITEM);
        verifyAction(profile, item, UnDefType.NULL, OnOffType.ON);
        verifyAction(profile, item, OnOffType.ON, OnOffType.OFF);
        verifyAction(profile, item, OnOffType.OFF, OnOffType.ON);
    }

    @Test
    public void testColorItem() {
        TriggerProfile profile = new RawButtonToggleProfile();
        ColorItem item = new ColorItem(TEST_ITEM);
        verifyAction(profile, item, UnDefType.NULL, OnOffType.ON);
        verifyAction(profile, item, HSBType.WHITE, OnOffType.OFF);
        verifyAction(profile, item, HSBType.BLACK, OnOffType.ON);
    }

    private void verifyAction(TriggerProfile profile, GenericItem item, State preCondition, Command expectation) {
        ItemChannelLink link = new ItemChannelLink(TEST_ITEM, CHANNEL_UID);
        ArgumentCaptor<ItemCommandEvent> eventCaptor = ArgumentCaptor.forClass(ItemCommandEvent.class);
        item.setState(preCondition);

        reset(eventPublisher);

        profile.onTrigger(eventPublisher, link, CommonTriggerEvents.PRESSED, item);

        verify(eventPublisher, times(1)).post(eventCaptor.capture());
        assertEquals(TEST_ITEM, eventCaptor.getValue().getItemName());
        assertEquals(expectation, eventCaptor.getValue().getItemCommand());
    }

}
