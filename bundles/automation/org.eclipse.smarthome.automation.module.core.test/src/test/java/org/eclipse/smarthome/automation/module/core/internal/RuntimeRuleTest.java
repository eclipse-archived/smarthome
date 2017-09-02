/**
  * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
package org.eclipse.smarthome.automation.module.core.internal;

import static org.junit.Assert.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent;
import org.eclipse.smarthome.automation.module.core.handler.CompareConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests the RuleEngine.
 *
 * @author Benedikt Niehues - initial contribution
 * @author Markus Rathgeb - Migrated Groovy tests to pure Java ones and made it more robust
 */
public class RuntimeRuleTest extends JavaOSGiTest {

    private final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class);
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

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
                return Arrays.asList(new Item[] { new SwitchItem("myMotionItem"), new SwitchItem("myPresenceItem"),
                        new SwitchItem("myLampItem"), new SwitchItem("myMotionItem2"),
                        new SwitchItem("myPresenceItem2"), new SwitchItem("myLampItem2"),
                        new SwitchItem("myMotionItem3"), new SwitchItem("myPresenceItem3"),
                        new SwitchItem("myLampItem3"), new SwitchItem("myMotionItem4"),
                        new SwitchItem("myPresenceItem4"), new SwitchItem("myLampItem4") });
            }
        });
        registerService(volatileStorageService);
    }

    @Test
    @Ignore
    public void testPredefinedRule() throws ItemNotFoundException, InterruptedException {
        final EventPublisher eventPublisher = getService(EventPublisher.class);
        // final ItemRegistry itemRegistry = getService(ItemRegistry.class);
        // final SwitchItem myMotionItem = (SwitchItem) itemRegistry.getItem("myMotionItem");
        // eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON));

        final Queue<Event> events = new LinkedList<>();

        registerService(new EventSubscriber() {
            @Override
            public void receive(final Event event) {
                logger.info("Event: {}", event.getTopic());
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Collections.singleton(ItemCommandEvent.TYPE);
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });

        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem", OnOffType.ON));

        waitForAssert(() -> {
            assertFalse(events.isEmpty());
            ItemCommandEvent event = (ItemCommandEvent) events.remove();
            assertEquals("smarthome/items/myLampItem/command", event.getTopic());
            assertEquals(OnOffType.ON, event.getItemCommand());
        });
    }

    @Test
    public void itemStateUpdatedBySimpleRule() throws ItemNotFoundException, InterruptedException {
        final Configuration triggerConfig = new Configuration(Stream
                .of(new SimpleEntry<>("eventSource", "myMotionItem2"), new SimpleEntry<>("eventTopic", "smarthome/*"),
                        new SimpleEntry<>("eventTypes", "ItemStateEvent"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Configuration actionConfig = new Configuration(
                Stream.of(new SimpleEntry<>("itemName", "myLampItem2"), new SimpleEntry<>("command", "ON"))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Rule rule = new Rule("myRule21" + new Random().nextInt());
        rule.setTriggers(Arrays.asList(
                new Trigger[] { new Trigger("ItemStateChangeTrigger2", "core.GenericEventTrigger", triggerConfig) }));
        rule.setActions(Arrays.asList(
                new Action[] { new Action("ItemPostCommandAction2", "core.ItemCommandAction", actionConfig, null) }));
        // I would expect the factory to create the UID of the rule and the name to be in the list of parameters.
        rule.setName("RuleByJAVA_API");

        logger.info("Rule created: {}", rule.getUID());

        final RuleRegistry ruleRegistry = getService(RuleRegistry.class);
        ruleRegistry.add(rule);
        ruleRegistry.setEnabled(rule.getUID(), true);

        waitForAssert(() -> {
            Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatusInfo(rule.getUID()).getStatus());
        });

        // Test rule
        final EventPublisher eventPublisher = getService(EventPublisher.class);
        Assert.assertNotNull(eventPublisher);

        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem2", OnOffType.ON));

        final Queue<Event> events = new LinkedList<>();

        registerService(new EventSubscriber() {
            @Override
            public void receive(final Event event) {
                logger.info("Event: {}", event.getTopic());
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Collections.singleton(ItemCommandEvent.TYPE);
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });

        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem2", OnOffType.ON));

        waitForAssert(() -> {
            assertFalse(events.isEmpty());
            ItemCommandEvent event = (ItemCommandEvent) events.remove();
            assertEquals("smarthome/items/myLampItem2/command", event.getTopic());
            assertEquals(OnOffType.ON, event.getItemCommand());
        });
    }

    @Test
    public void modeTypesRegistration() {
        final ModuleTypeRegistry mtr = getService(ModuleTypeRegistry.class);
        waitForAssert(() -> {
            Assert.assertNotNull(mtr.get("core.GenericEventTrigger"));
            Assert.assertNotNull(mtr.get("core.GenericEventCondition"));
            Assert.assertNotNull(mtr.get("core.ItemStateChangeTrigger"));
            Assert.assertNotNull(mtr.get("core.ItemStateUpdateTrigger"));
            Assert.assertNotNull(mtr.get(CompareConditionHandler.MODULE_TYPE));
        });
    }

    private Configuration newRightOperatorConfig(final Object right, final Object operator) {
        return new Configuration(Stream.of(new SimpleEntry<>("right", right), new SimpleEntry<>("operator", operator))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    private Configuration newRightOperatorInputPropertyConfig(final Object right, final Object operator,
            final Object inputProperty) {
        return new Configuration(Stream
                .of(new SimpleEntry<>("right", right), new SimpleEntry<>("operator", operator),
                        new SimpleEntry<>("inputproperty", inputProperty))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    private void assertSatisfiedHandlerInput(final CompareConditionHandler handler, final boolean expected,
            final Object input) {
        final boolean is = handler.isSatisfied(Collections.singletonMap("input", input));
        if (expected) {
            Assert.assertTrue(is);
        } else {
            Assert.assertFalse(is);
        }
    }

    @Test
    public void compareConditionWorks() {
        final Configuration conditionConfig = newRightOperatorConfig("ON", "=");
        final Map<String, String> inputs = Stream.of(new SimpleEntry<>("input", "someTrigger.someoutput"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        final Condition condition = new Condition("id", "core.GenericCompareCondition", conditionConfig, inputs);
        final CompareConditionHandler handler = new CompareConditionHandler(condition);

        assertSatisfiedHandlerInput(handler, true, OnOffType.ON);
        assertSatisfiedHandlerInput(handler, true, "ON");
        assertSatisfiedHandlerInput(handler, false, OnOffType.OFF);
        assertSatisfiedHandlerInput(handler, false, "OFF");

        condition.setConfiguration(newRightOperatorConfig("21", "="));
        assertSatisfiedHandlerInput(handler, true, 21);
        assertSatisfiedHandlerInput(handler, false, 22);

        condition.setConfiguration(newRightOperatorConfig("21", "<"));
        assertSatisfiedHandlerInput(handler, true, 20);
        assertSatisfiedHandlerInput(handler, false, 22);

        assertSatisfiedHandlerInput(handler, true, 20l);
        assertSatisfiedHandlerInput(handler, false, 22l);

        assertSatisfiedHandlerInput(handler, true, 20.9d);
        assertSatisfiedHandlerInput(handler, false, 21.1d);

        condition.setConfiguration(newRightOperatorConfig("21", "<="));
        assertSatisfiedHandlerInput(handler, true, 20);
        assertSatisfiedHandlerInput(handler, true, 21);
        assertSatisfiedHandlerInput(handler, false, 22);

        assertSatisfiedHandlerInput(handler, true, 20l);
        assertSatisfiedHandlerInput(handler, true, 21l);
        assertSatisfiedHandlerInput(handler, false, 22l);

        assertSatisfiedHandlerInput(handler, true, 20.9d);
        assertSatisfiedHandlerInput(handler, true, 21.0d);
        assertSatisfiedHandlerInput(handler, false, 21.1d);

        condition.setConfiguration(newRightOperatorConfig("21", "<"));
        assertSatisfiedHandlerInput(handler, true, 20);
        assertSatisfiedHandlerInput(handler, false, 22);

        assertSatisfiedHandlerInput(handler, true, 20l);
        assertSatisfiedHandlerInput(handler, false, 22l);

        assertSatisfiedHandlerInput(handler, true, 20.9d);
        assertSatisfiedHandlerInput(handler, false, 21.1d);

        condition.setConfiguration(newRightOperatorConfig("21", "<="));
        assertSatisfiedHandlerInput(handler, true, 20);
        assertSatisfiedHandlerInput(handler, true, 21);
        assertSatisfiedHandlerInput(handler, false, 22);

        assertSatisfiedHandlerInput(handler, true, 20l);
        assertSatisfiedHandlerInput(handler, true, 21l);
        assertSatisfiedHandlerInput(handler, false, 22l);

        assertSatisfiedHandlerInput(handler, true, 20.9d);
        assertSatisfiedHandlerInput(handler, true, 21.0d);
        assertSatisfiedHandlerInput(handler, false, 21.1d);

        condition.setConfiguration(newRightOperatorConfig(".*anything.*", "matches"));
        assertSatisfiedHandlerInput(handler, false, "something matches?");
        assertSatisfiedHandlerInput(handler, true, "anything matches?");

        Assert.assertFalse(handler.isSatisfied(Stream.of(new SimpleEntry<>("nothing", "nothing"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))));

        condition.setConfiguration(newRightOperatorConfig("ONOFF", "matches"));
        assertSatisfiedHandlerInput(handler, false, OnOffType.ON);

        final Event event = ItemEventFactory.createStateEvent("itemName", OnOffType.OFF, "source");
        condition.setConfiguration(newRightOperatorInputPropertyConfig(".*ON.*", "matches", "itemName"));
        assertSatisfiedHandlerInput(handler, false, event);
        condition.setConfiguration(newRightOperatorInputPropertyConfig("itemName", "matches", "itemName"));
        assertSatisfiedHandlerInput(handler, true, event);

        condition.setConfiguration(newRightOperatorConfig("null", "="));
        assertSatisfiedHandlerInput(handler, true, null);
        condition.setConfiguration(newRightOperatorConfig("notnull", "="));
        assertSatisfiedHandlerInput(handler, false, null);
        condition.setConfiguration(newRightOperatorConfig("ON", "<"));
        assertSatisfiedHandlerInput(handler, false, OnOffType.ON);

        condition.setConfiguration(newRightOperatorInputPropertyConfig("ON", "<", "nothing"));
        assertSatisfiedHandlerInput(handler, false, event);
        condition.setConfiguration(newRightOperatorInputPropertyConfig("ON", "=", "nothing"));
        assertSatisfiedHandlerInput(handler, true, "ON");
    }

    @Test
    public void ruleTriggeredByCompositeTrigger() throws ItemNotFoundException, InterruptedException {
        // //Test the creation of a rule out of
        final Configuration triggerConfig = new Configuration(Stream.of(new SimpleEntry<>("itemName", "myMotionItem3"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Configuration actionConfig = new Configuration(
                Stream.of(new SimpleEntry<>("itemName", "myLampItem3"), new SimpleEntry<>("command", "ON"))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        final Rule rule = new Rule("myRule21" + new Random().nextInt() + "_COMPOSITE");
        rule.setTriggers(Arrays.asList(new Trigger[] {
                new Trigger("ItemStateChangeTrigger3", "core.ItemStateChangeTrigger", triggerConfig) }));
        rule.setActions(Arrays.asList(
                new Action[] { new Action("ItemPostCommandAction3", "core.ItemCommandAction", actionConfig, null) }));
        rule.setName("RuleByJAVA_API_WithCompositeTrigger");

        logger.info("Rule created: {}", rule.getUID());

        final RuleRegistry ruleRegistry = getService(RuleRegistry.class);
        ruleRegistry.add(rule);

        // Test rule

        waitForAssert(() -> {
            Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatusInfo(rule.getUID()).getStatus());
        });

        final Queue<Event> events = new LinkedList<>();

        registerService(new EventSubscriber() {
            @Override
            public void receive(final Event event) {
                logger.info("RuleEvent: {}", event.getTopic());
                events.add(event);
            }

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Collections.singleton(RuleStatusInfoEvent.TYPE);
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }
        });

        final EventPublisher eventPublisher = getService(EventPublisher.class);
        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem3", OnOffType.ON));
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem3", OnOffType.ON));

        waitForAssert(() -> {
            assertFalse(events.isEmpty());
            RuleStatusInfoEvent event = (RuleStatusInfoEvent) events.remove();
            assertEquals(RuleStatus.RUNNING, event.getStatusInfo().getStatus());
        });

        waitForAssert(() -> {
            assertFalse(events.isEmpty());
            RuleStatusInfoEvent event = (RuleStatusInfoEvent) events.remove();
            assertEquals(RuleStatus.IDLE, event.getStatusInfo().getStatus());
        });
    }

    @Test
    @Ignore
    public void ruleEnableHandlerWorks() throws ItemNotFoundException {
        final RuleRegistry ruleRegistry = getService(RuleRegistry.class);
        final String firstRuleUID = "FirstTestRule";
        final String secondRuleUID = "SecondTestRule";
        final String thirdRuleUID = "ThirdTestRule";
        final String[] firstConfig = new String[] { "FirstTestRule", "SecondTestRule" };
        final String[] secondConfig = new String[] { "FirstTestRule" };

        final String firstRuleAction = "firstRuleAction";
        final String secondRuleAction = "secondRuleAction";

        try {
            final Configuration triggerConfig = new Configuration(
                    Stream.of(new SimpleEntry<>("itemName", "myMotionItem3"))
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
            final Configuration actionConfig = new Configuration(
                    Stream.of(new SimpleEntry<>("enable", false), new SimpleEntry<>("ruleUIDs", firstConfig))
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

            final Rule rule = new Rule(firstRuleAction);
            rule.setTriggers(Arrays.asList(new Trigger[] {
                    new Trigger("ItemStateChangeTrigger3", "core.ItemStateChangeTrigger", triggerConfig) }));
            rule.setActions(Arrays.asList(
                    new Action[] { new Action("RuleAction", "core.RuleEnablementAction", actionConfig, null) }));

            ruleRegistry.add(new Rule(firstRuleUID));
            ruleRegistry.add(new Rule(secondRuleUID));
            ruleRegistry.add(new Rule(thirdRuleUID));
            ruleRegistry.add(rule);

            final ItemRegistry itemRegistry = getService(ItemRegistry.class);
            final EventPublisher eventPublisher = getService(EventPublisher.class);
            final Item myMotionItem = itemRegistry.getItem("myMotionItem3");
            eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem3",
                    TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")));

            waitForAssert(() -> {
                Assert.assertEquals(RuleStatus.DISABLED, ruleRegistry.getStatus(firstRuleUID));
                Assert.assertEquals(RuleStatus.DISABLED, ruleRegistry.getStatus(secondRuleUID));
                Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatus(thirdRuleUID));
            });

            final Configuration triggerConfig2 = new Configuration(
                    Stream.of(new SimpleEntry<>("itemName", "myMotionItem3"))
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
            final Configuration actionConfig2 = new Configuration(
                    Stream.of(new SimpleEntry<>("enable", true), new SimpleEntry<>("ruleUIDs", secondConfig))
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

            final Rule rule2 = new Rule(secondRuleAction);
            rule2.setTriggers(Arrays.asList(new Trigger[] {
                    new Trigger("ItemStateChangeTrigger3", "core.ItemStateChangeTrigger", triggerConfig2) }));
            rule2.setActions(Arrays.asList(
                    new Action[] { new Action("RuleAction", "core.RuleEnablementAction", actionConfig2, null) }));
            ruleRegistry.add(rule2);

            eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem3",
                    TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "OFF")));

            waitForAssert(() -> {
                Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatus(firstRuleUID));
                Assert.assertEquals(RuleStatus.DISABLED, ruleRegistry.getStatus(secondRuleUID));
                Assert.assertEquals(RuleStatus.IDLE, ruleRegistry.getStatus(thirdRuleUID));
            });
        } finally {
            ruleRegistry.remove(firstRuleUID);
            ruleRegistry.remove(secondRuleUID);
            ruleRegistry.remove(thirdRuleUID);
            ruleRegistry.remove(firstRuleAction);
            ruleRegistry.remove(secondRuleAction);
        }
    }

}
