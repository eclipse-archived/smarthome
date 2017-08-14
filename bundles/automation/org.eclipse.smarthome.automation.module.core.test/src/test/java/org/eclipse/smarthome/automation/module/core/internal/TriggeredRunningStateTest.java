/**
 * Copyright (c) 2015, 2017 by Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.internal;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests the triggered and running states of rules.
 *
 * @author Yordan Mihaylov - initial contribution
 */
public class TriggeredRunningStateTest extends JavaOSGiTest {

    private final Logger logger = LoggerFactory.getLogger(TriggeredRunningStateTest.class);
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();
    final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();

    @Before
    public void before() {
        registerService(new ItemProvider() {
            @Override
            public void addProviderChangeListener(final ProviderChangeListener<Item> listener) {
            }

            @Override
            public void removeProviderChangeListener(final ProviderChangeListener<Item> listener) {
            }

            @Override
            public Collection<Item> getAll() {
                return Arrays.asList(new Item[] { new SwitchItem("myMotionItemTrigger"),
                        new SwitchItem("myLampItemAction"), new SwitchItem("myLampItemCondition") });
            }
        });
        registerService(volatileStorageService);
        enableItemAutoUpdate();

        registerService(new EventSubscriber() {
            @Override
            public void receive(final Event event) {
                logger.info("Event: {}", event.getTopic());
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Stream.of(RuleStatusInfoEvent.TYPE, ItemStateEvent.TYPE).collect(Collectors.toSet());
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });

    }

    @Test
    public void testRunningState() throws ItemNotFoundException, InterruptedException {

        final RuleRegistry ruleRegistry = getService(RuleRegistry.class);
        Rule rule = prepareRule();
        ruleRegistry.add(rule);
        ruleRegistry.setEnabled(rule.getUID(), true);

        waitForAssert(() -> {
            Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatusInfo(rule.getUID()).getStatus());
        });

        // prepare condition item state - the condition must be satisfied
        final ItemRegistry itemRegistry = getService(ItemRegistry.class);
        SwitchItem myLampItem = (SwitchItem) itemRegistry.getItem("myLampItemCondition");
        myLampItem.send(OnOffType.ON);

        Assert.assertNotNull(myLampItem);
        waitForAssert(() -> {
            Assert.assertEquals(OnOffType.ON, myLampItem.getState());
        });

        // prepare action item state
        final SwitchItem myLampItemAction = (SwitchItem) itemRegistry.getItem("myLampItemAction");
        myLampItemAction.send(OnOffType.OFF);

        // Test rule
        SwitchItem myMotionItem = (SwitchItem) itemRegistry.getItem("myMotionItemTrigger");

        myMotionItem.send(OnOffType.ON);

        boolean ruleStatusInfoEventFound = false;
        boolean triggeredEventFound = false;
        boolean runningEventFound = false;
        boolean itemStateEventFound = false;
        String ruleStatusTopic = "smarthome/rules/{ruleID}/state".replace("{ruleID}", rule.getUID());
        long wait = TimeUnit.MILLISECONDS.toNanos(DFL_TIMEOUT);
        final long nanoEnd = System.nanoTime() + wait;
        do {
            final Event event = events.poll(wait, TimeUnit.NANOSECONDS);

            Assert.assertNotNull(event);
            wait = nanoEnd - System.nanoTime();

            if (ruleStatusTopic.equals(event.getTopic())) {
                if (!(event instanceof RuleStatusInfoEvent)) {
                    continue;
                }

                final RuleStatusInfoEvent ruleStateEvent = (RuleStatusInfoEvent) event;
                switch (ruleStateEvent.getStatusInfo().getStatus()) {
                    case TRIGGERED:
                        triggeredEventFound = true;
                        break;
                    case RUNNING:
                        runningEventFound = triggeredEventFound;
                        break;
                    case IDLE:
                        ruleStatusInfoEventFound = runningEventFound;
                        break;
                }

            } else if ("smarthome/items/myLampItemAction/state".equals(event.getTopic())) {
                if (!(event instanceof ItemStateEvent)) {
                    continue;
                }

                final ItemStateEvent itemEvent = (ItemStateEvent) event;
                if (itemEvent.getItemState() != OnOffType.ON) {
                    continue;
                }
                itemStateEventFound = true;
            }
        } while (!ruleStatusInfoEventFound || !itemStateEventFound && wait > 0);
        Assert.assertTrue(ruleStatusInfoEventFound);
        Assert.assertTrue(itemStateEventFound);

        final Item myLampItemAction2 = itemRegistry.getItem("myLampItemAction");
        Assert.assertNotNull(myLampItemAction2);
        waitForAssert(() -> {
            Assert.assertEquals(OnOffType.ON, myLampItemAction2.getState());
        });

    }

    @Test
    public void testTriggeredState() throws ItemNotFoundException, InterruptedException {

        final RuleRegistry ruleRegistry = getService(RuleRegistry.class);
        Rule rule = prepareRule();
        ruleRegistry.add(rule);
        ruleRegistry.setEnabled(rule.getUID(), true);

        waitForAssert(() -> {
            Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatusInfo(rule.getUID()).getStatus());
        });

        // prepare condition item state - the condition must be unsatisfied
        final ItemRegistry itemRegistry = getService(ItemRegistry.class);
        SwitchItem myLampItem = (SwitchItem) itemRegistry.getItem("myLampItemCondition");
        myLampItem.send(OnOffType.OFF);

        Assert.assertNotNull(myLampItem);
        waitForAssert(() -> {
            Assert.assertEquals(OnOffType.OFF, myLampItem.getState());
        });

        // prepare action item state
        final SwitchItem myLampItemAction = (SwitchItem) itemRegistry.getItem("myLampItemAction");
        myLampItemAction.send(OnOffType.OFF);

        Assert.assertNotNull(myLampItemAction);
        waitForAssert(() -> {
            Assert.assertEquals(OnOffType.OFF, myLampItemAction.getState());
        });

        // Test rule
        SwitchItem myMotionItem = (SwitchItem) itemRegistry.getItem("myMotionItemTrigger");

        myMotionItem.send(OnOffType.ON);

        boolean ruleStatusInfoEventFound = false;
        boolean triggeredEventFound = false;
        String ruleStatusTopic = "smarthome/rules/{ruleID}/state".replace("{ruleID}", rule.getUID());
        long wait = TimeUnit.MILLISECONDS.toNanos(DFL_TIMEOUT);
        final long nanoEnd = System.nanoTime() + wait;
        do {
            final Event event = events.poll(wait, TimeUnit.NANOSECONDS);

            Assert.assertNotNull(event);
            wait = nanoEnd - System.nanoTime();

            if (ruleStatusTopic.equals(event.getTopic())) {
                if (!(event instanceof RuleStatusInfoEvent)) {
                    continue;
                }

                final RuleStatusInfoEvent ruleStateEvent = (RuleStatusInfoEvent) event;
                switch (ruleStateEvent.getStatusInfo().getStatus()) {
                    case TRIGGERED:
                        triggeredEventFound = true;
                        break;
                    case IDLE:
                        ruleStatusInfoEventFound = triggeredEventFound;
                        break;
                }
            }
        } while (!ruleStatusInfoEventFound && wait > 0);
        Assert.assertTrue(ruleStatusInfoEventFound);
        Assert.assertNotNull(myLampItemAction);
        Assert.assertEquals(OnOffType.OFF, myLampItemAction.getState());
    }

    private Rule prepareRule() {
        final Configuration triggerConfig = new Configuration(Stream
                .of(new SimpleEntry<>("eventSource", "myMotionItemTrigger"),
                        new SimpleEntry<>("eventTopic", "smarthome/*"),
                        new SimpleEntry<>("eventTypes", "ItemStateEvent"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

        final Configuration conditionConfig = new Configuration(Stream
                .of(new SimpleEntry<>("itemName", "myLampItemCondition"), new SimpleEntry<>("operator", "="),
                        new SimpleEntry<>("state", "ON"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

        final Configuration actionConfig = new Configuration(
                Stream.of(new SimpleEntry<>("itemName", "myLampItemAction"), new SimpleEntry<>("command", "ON"))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

        final Rule rule = new Rule("myRule21" + new Random().nextInt());
        rule.setTriggers(Arrays.asList(
                new Trigger[] { new Trigger("ItemStateChangeTrigger2", "core.GenericEventTrigger", triggerConfig) }));
        rule.setConditions(Arrays.asList(new Condition[] {
                new Condition("ItemStateCondition2", "core.ItemStateCondition", conditionConfig, null) }));
        rule.setActions(Arrays.asList(
                new Action[] { new Action("ItemPostCommandAction2", "core.ItemCommandAction", actionConfig, null) }));
        // I would expect the factory to create the UID of the rule and the name to be in the list of parameters.
        rule.setName("RuleByJAVA_API");

        String ruleUID = rule.getUID();
        logger.info("Rule created: {}", ruleUID);
        return rule;
    }
}
