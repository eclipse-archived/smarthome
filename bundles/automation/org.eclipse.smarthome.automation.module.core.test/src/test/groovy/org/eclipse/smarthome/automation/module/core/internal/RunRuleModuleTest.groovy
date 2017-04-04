/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.internal;


import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.module.core.handler.CompareConditionHandler
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemCommandEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.TypeParser
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets
/**
 * this tests the RunRuleAction
 *
 * @author Benedikt Niehues - initial contribution
 *
 */
class RunRuleModuleTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    void before() {

        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("switch1"),
                    new SwitchItem("switch2"),
                    new SwitchItem("switch3"),
                    new SwitchItem("ruleTrigger")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerService(volatileStorageService)

        enableItemAutoUpdate()
    }



    @Test
    public void 'assert that a scene is activated by a rule'() {
        //Creation of scene RULE
        def sceneRuleAction1Config = new Configuration([itemName:"switch1", command:"ON"])
        def sceneRuleAction2Config = new Configuration([itemName:"switch2", command:"ON"])
        def sceneRuleAction3Config = new Configuration([itemName:"switch3", command:"ON"])
        def sceneRuleActions = [
            new Action("sceneItemPostCommandAction1","core.ItemCommandAction",sceneRuleAction1Config,null),
            new Action("sceneItemPostCommandAction2","core.ItemCommandAction",sceneRuleAction2Config,null),
            new Action("sceneItemPostCommandAction3","core.ItemCommandAction",sceneRuleAction3Config,null)
        ]
        def sceneRule = new Rule("exampleSceneRule")
        sceneRule.actions=sceneRuleActions
        sceneRule.name="Example Scene"
        logger.info("SceneRule created: "+sceneRule.getUID())
        //creation of outer rule
        def outerRuleTriggerConfig = new Configuration([eventSource:"ruleTrigger", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def outerRuleActionConfig = new Configuration([ruleUIDs:"[exampleSceneRule]"])
        def outerRuleTriggers = [
            new Trigger("ItemStateChangeTrigger2", "core.GenericEventTrigger", outerRuleTriggerConfig)
        ]
        def outerRuleActions = [
            new Action("RunRuleAction1", "core.RunRuleAction", outerRuleActionConfig, null)
        ]
        def outerRule = new Rule("sceneActivationRule")
        outerRule.triggers = outerRuleTriggers
        outerRule.actions = outerRuleActions
        outerRule.name="scene activator"

        logger.info("SceneActivationRule created: "+outerRule.getUID())

        def ruleRegistry = getService(RuleRegistry) as RuleRegistry
        ruleRegistry.add(sceneRule)
        ruleRegistry.setEnabled(sceneRule.UID, true)
        ruleRegistry.add(outerRule)
        ruleRegistry.setEnabled(outerRule.UID, true)

        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(outerRule.UID).status, is(RuleStatus.IDLE)
            assertThat ruleRegistry.getStatusInfo(sceneRule.UID).status, is(RuleStatus.IDLE)
        })
        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem ruleTriggerItem = itemRegistry.getItem("ruleTrigger")

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("switch3")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //trigger rule by switching triggerItem ON
        ruleTriggerItem.send(OnOffType.ON)

        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/switch3/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))

        def switch1 = itemRegistry.getItem("switch1")
        def switch2 = itemRegistry.getItem("switch2")
        def switch3 = itemRegistry.getItem("switch3")
        assertThat switch1, is(notNullValue())
        assertThat switch2, is(notNullValue())
        assertThat switch3, is(notNullValue())

        assertThat switch1.state, is(OnOffType.ON)
        assertThat switch2.state, is(OnOffType.ON)
        assertThat switch3.state, is(OnOffType.ON)
    }
}