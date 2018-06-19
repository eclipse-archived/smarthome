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
package org.eclipse.smarthome.automation.module.timer.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleManager;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.ModuleBuilder;
import org.eclipse.smarthome.automation.core.util.RuleBuilder;
import org.eclipse.smarthome.automation.module.core.handler.ItemCommandActionHandler;
import org.eclipse.smarthome.automation.module.core.handler.ItemStateTriggerHandler;
import org.eclipse.smarthome.automation.module.timer.handler.DayOfWeekConditionHandler;
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
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this tests the dayOfWeek Condition
 *
 * @author Kai Kreuzer - initial contribution
 *
 */
public class DayOfWeekConditionHandlerTest extends JavaOSGiTest {

    final Logger logger = LoggerFactory.getLogger(DayOfWeekConditionHandlerTest.class);
    VolatileStorageService volatileStorageService = new VolatileStorageService();
    RuleRegistry ruleRegistry;
    RuleManager ruleEngine;
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.ENGLISH);
    String dayOfWeek = sdf.format(cal.getTime()).toUpperCase();
    Event itemEvent = null;

    public DayOfWeekConditionHandlerTest() {
    }

    @Before
    public void before() {
        logger.info("Today is {}", dayOfWeek);

        ItemProvider itemProvider = new ItemProvider() {
            @Override
            public void addProviderChangeListener(@NonNull ProviderChangeListener<@NonNull Item> listener) {
            }

            @Override
            public @NonNull Collection<@NonNull Item> getAll() {
                List<Item> items = new ArrayList<>();
                items.add(new SwitchItem("TriggeredItem"));
                items.add(new SwitchItem("SwitchedItem"));
                return items;
            }

            @Override
            public void removeProviderChangeListener(@NonNull ProviderChangeListener<@NonNull Item> listener) {
            }

        };
        registerService(itemProvider);
        registerService(volatileStorageService);
        waitForAssert(() -> {
            ruleRegistry = getService(RuleRegistry.class);
            assertThat(ruleRegistry, is(notNullValue()));
        }, 3000, 100);
        waitForAssert(() -> {
            ruleEngine = getService(RuleManager.class);
            assertThat(ruleEngine, is(notNullValue()));
        }, 3000, 100);
    }

    @Test
    public void assertThatConditionWorks() {
        Configuration conditionConfiguration = new Configuration(Collections.singletonMap("days",
                Arrays.asList(new String[] { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" })));
        Condition condition = ModuleBuilder.createCondition().withId("id")
                .withTypeUID(DayOfWeekConditionHandler.MODULE_TYPE_ID).withConfiguration(conditionConfiguration)
                .build();
        DayOfWeekConditionHandler handler = new DayOfWeekConditionHandler(condition);

        assertThat(handler.isSatisfied(null), is(true));

        condition = ModuleBuilder.createCondition(condition)
                .withConfiguration(new Configuration(Collections.singletonMap("days", Collections.emptyList())))
                .build();
        handler = new DayOfWeekConditionHandler(condition);
        assertThat(handler.isSatisfied(null), is(false));

        condition = ModuleBuilder.createCondition(condition)
                .withConfiguration(
                        new Configuration(Collections.singletonMap("days", Collections.singletonList(dayOfWeek))))
                .build();
        handler = new DayOfWeekConditionHandler(condition);
        assertThat(handler.isSatisfied(null), is(true));
    }

    @Test
    public void checkIfModuleTypeIsRegistered() {
        ModuleTypeRegistry mtr = getService(ModuleTypeRegistry.class);
        waitForAssert(() -> {
            assertThat(mtr.get(DayOfWeekConditionHandler.MODULE_TYPE_ID), is(notNullValue()));
        }, 3000, 100);
    }

    @Test
    public void assertThatConditionWorksInRule() throws ItemNotFoundException {
        String testItemName1 = "TriggeredItem";
        String testItemName2 = "SwitchedItem";

        ItemRegistry itemRegistry = getService(ItemRegistry.class);
        SwitchItem triggeredItem = (SwitchItem) itemRegistry.getItem(testItemName1);
        SwitchItem switchedItem = (SwitchItem) itemRegistry.getItem(testItemName2);

        /*
         * Create Rule
         */
        logger.info("Create rule");
        Configuration triggerConfig = new Configuration(Collections.singletonMap("itemName", testItemName1));
        List<Trigger> triggers = Collections.singletonList(ModuleBuilder.createTrigger().withId("MyTrigger")
                .withTypeUID(ItemStateTriggerHandler.UPDATE_MODULE_TYPE_ID).withConfiguration(triggerConfig).build());

        Configuration conditionConfig = new Configuration(Collections.singletonMap("days", dayOfWeek));
        List<Condition> conditions = Collections.singletonList(ModuleBuilder.createCondition().withId("MyDOWCondition")
                .withTypeUID(DayOfWeekConditionHandler.MODULE_TYPE_ID).withConfiguration(conditionConfig).build());

        Map<String, Object> cfgEntries = new HashMap<>();
        cfgEntries.put("itemName", testItemName2);
        cfgEntries.put("command", "ON");
        Configuration actionConfig = new Configuration(cfgEntries);
        List<Action> actions = Collections.singletonList(ModuleBuilder.createAction().withId("MyItemPostCommandAction")
                .withTypeUID(ItemCommandActionHandler.ITEM_COMMAND_ACTION).withConfiguration(actionConfig).build());

        // prepare the execution
        EventPublisher eventPublisher = getService(EventPublisher.class);

        EventSubscriber itemEventHandler = new EventSubscriber() {

            @Override
            public Set<String> getSubscribedEventTypes() {
                return Collections.singleton(ItemCommandEvent.TYPE);
            }

            @Override
            public EventFilter getEventFilter() {
                return null;
            }

            @Override
            public void receive(Event event) {
                logger.info("Event: {}", event.getTopic());
                if (event.getTopic().contains(testItemName2)) {
                    DayOfWeekConditionHandlerTest.this.itemEvent = event;
                }
            }
        };
        registerService(itemEventHandler);

        Rule rule = RuleBuilder.create("MyRule" + new Random().nextInt()).withTriggers(triggers)
                .withConditions(conditions).withActions(actions).withName("MyDOWConditionTestRule").build();
        logger.info("Rule created: {}", rule.getUID());

        logger.info("Add rule");
        ruleRegistry.add(rule);
        logger.info("Rule added");

        logger.info("Enable rule and wait for idle status");
        ruleEngine.setEnabled(rule.getUID(), true);
        waitForAssert(() -> {
            final RuleStatusInfo ruleStatus = ruleEngine.getStatusInfo(rule.getUID());
            assertThat(ruleStatus.getStatus(), is(RuleStatus.IDLE));
        });
        logger.info("Rule is enabled and idle");

        logger.info("Send and wait for item state is ON");
        eventPublisher.post(ItemEventFactory.createStateEvent(testItemName1, OnOffType.ON));
        waitForAssert(() -> {
            assertThat(itemEvent, is(notNullValue()));
            assertThat(((ItemCommandEvent) itemEvent).getItemCommand(), is(OnOffType.ON));
        });
        logger.info("item state is ON");

        // now make the condition fail
        Rule rule2 = RuleBuilder.create(rule)
                .withConditions(ModuleBuilder.createCondition(rule.getConditions().get(0))
                        .withConfiguration(new Configuration(Collections.singletonMap("days", Collections.emptyList())))
                        .build())
                .build();
        ruleRegistry.update(rule2);

        // prepare the execution
        itemEvent = null;
        eventPublisher.post(ItemEventFactory.createStateEvent(testItemName1, OnOffType.ON));
        waitForAssert(() -> {
            assertThat(itemEvent, is(nullValue()));
        });
    }
}
