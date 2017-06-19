/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.service.component.ComponentContext;

/**
 * Tests for {@link ChannelStateDescriptionProvider}.
 *
 * @author Alex Tugarev - Initial contribution
 * @author Thomas Höfer - Thing type constructor modified because of thing properties introduction
 * @author Markus Rathgeb - Migrated from Groovy to plain Java
 */
public class ChannelStateDescriptionProviderOSGiTest extends JavaOSGiTest {

    private ItemRegistry itemRegistry;
    private ItemChannelLinkRegistry linkRegistry;
    private StateDescriptionProvider stateDescriptionProvider;

    @Mock
    private ComponentContext componentContext;

    @Before
    public void setup() {
        initMocks(this);

        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);

        registerVolatileStorageService();

        itemRegistry = getService(ItemRegistry.class);
        Assert.assertNotNull(itemRegistry);

        final TestThingHandlerFactory thingHandlerFactory = new TestThingHandlerFactory();
        thingHandlerFactory.activate(componentContext);
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName());

        final StateDescription state = new StateDescription(BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.TEN,
                "%d Peek", true, Collections.singletonList(new StateOption("SOUND", "My great sound.")));

        final ChannelType channelType = new ChannelType(new ChannelTypeUID("hue:alarm"), false, "Number", " ", "", null,
                null, state, null);
        final Collection<ChannelType> channelTypes = Collections.singleton(channelType);

        registerService(new ChannelTypeProvider() {
            @Override
            public Collection<ChannelType> getChannelTypes(Locale locale) {
                return channelTypes;
            }

            @Override
            public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
                for (final ChannelType channelType : channelTypes) {
                    if (channelType.getUID().equals(channelTypeUID)) {
                        return channelType;
                    }
                }
                return null;
            }

            @Override
            public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
                return null;
            }

            @Override
            public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
                return Collections.emptySet();
            }
        });

        registerService(new SimpleThingTypeProvider(Collections.singleton(new ThingType(new ThingTypeUID("hue:lamp"),
                null, " ", null, Collections.singletonList(new ChannelDefinition("1", channelType.getUID())), null,
                null, null))));

        registerService(new TestItemProvider(Collections.singleton(new NumberItem("TestItem"))));

        linkRegistry = getService(ItemChannelLinkRegistry.class);

        stateDescriptionProvider = getService(StateDescriptionProvider.class);
        Assert.assertNotNull(stateDescriptionProvider);
    }

    @After
    public void teardown() {
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);
        managedThingProvider.getAll().forEach(thing -> {
            managedThingProvider.remove(thing.getUID());
        });
        linkRegistry.getAll().forEach(link -> {
            linkRegistry.remove(link.getID());
        });
    }

    /**
     * Assert that item's state description is present.
     */
    @Test
    public void presentItemStateDescription() {
        ThingRegistry thingRegistry = getService(ThingRegistry.class);
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);

        Thing thing = thingRegistry.createThingOfType(new ThingTypeUID("hue:lamp"), new ThingUID("hue:lamp:lamp1"),
                null, "test thing", new Configuration());
        Assert.assertNotNull(thing);
        managedThingProvider.add(thing);
        final ItemChannelLink link = new ItemChannelLink("TestItem", thing.getChannels().get(0).getUID());
        linkRegistry.add(link);
        //
        final Collection<Item> items = itemRegistry.getItems();
        Assert.assertEquals(false, items.isEmpty());

        Item numberItem = null;
        for (final Item item : items) {
            if (item.getType().equals("Number")) {
                numberItem = item;
                break;
            }
        }
        Assert.assertNotNull(numberItem);

        final StateDescription state = numberItem.getStateDescription();
        Assert.assertNotNull(state);

        Assert.assertEquals(BigDecimal.ZERO, state.getMinimum());
        Assert.assertEquals(BigDecimal.valueOf(100), state.getMaximum());
        Assert.assertEquals(BigDecimal.TEN, state.getStep());
        Assert.assertEquals("%d Peek", state.getPattern());
        Assert.assertEquals(true, state.isReadOnly());
        List<StateOption> opts = state.getOptions();
        Assert.assertEquals(1, opts.size());
        final StateOption opt = opts.get(0);
        Assert.assertEquals("SOUND", opt.getValue());
        Assert.assertEquals("My great sound.", opt.getLabel());

    }

    /*
     * Helper
     */

    class TestThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public void activate(final ComponentContext ctx) {
            super.activate(ctx);
        }

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true;
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new BaseThingHandler(thing) {
                @Override
                public void handleCommand(ChannelUID channelUID, Command command) {
                }
            };
        }
    }

    class TestItemProvider implements ItemProvider {
        private Collection<Item> items;

        TestItemProvider(Collection<Item> items) {
            this.items = items;
        }

        @Override
        public void addProviderChangeListener(ProviderChangeListener<Item> listener) {
        }

        @Override
        public Collection<Item> getAll() {
            return items;
        }

        @Override
        public void removeProviderChangeListener(ProviderChangeListener<Item> listener) {
        }
    }
}