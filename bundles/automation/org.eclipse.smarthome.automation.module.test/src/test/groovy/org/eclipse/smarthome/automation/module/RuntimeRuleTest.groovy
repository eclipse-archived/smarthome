/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module;


import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.hamcrest.core.IsEqual;
import org.junit.Before
import org.junit.Ignore;
import org.junit.Test
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets;;;

class RuntimeRuleTest extends OSGiTest{

	final Logger logger = LoggerFactory.getLogger(RuntimeRuleTest.class)
	VolatileStorageService volatileStorageService = new VolatileStorageService()

	@Before
	void before() {
		def itemProvider = [
			getAll: {
				[
					new SwitchItem("myMotionItem"),
					new SwitchItem("myPresenceItem"),
					new SwitchItem("myLampItem"),
					new SwitchItem("myMotionItem2"),
					new SwitchItem("myPresenceItem2"),
					new SwitchItem("myLampItem2")
				]
			},
			addProviderChangeListener: {},
			removeProviderChangeListener: {},
			allItemsChanged: {}] as ItemProvider
		registerService(itemProvider)
		registerService(volatileStorageService)

		def autoupdateConfig = [
			autoUpdate: { String itemName -> return true }

		] as AutoUpdateBindingConfigProvider
		registerService(autoupdateConfig)
	}


	@Test
	@Ignore
	public void testPredefinedRule() {

		def EventPublisher eventPublisher = getService(EventPublisher)
		def ItemRegistry itemRegistry = getService(ItemRegistry)
		SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem")
		eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem", OnOffType.ON))
		Event event = null
		def eventHandler = [
			receive: { Event e ->
				logger.info("Event: " + e.topic)
				event=e
			},

			getSubscribedEventTypes: {
				Sets.newHashSet(ItemCommandEvent.TYPE, ItemStateEvent.TYPE)
			},

			getEventFilter:{ null }

		] as EventSubscriber

		registerService(eventHandler)
		eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem", OnOffType.ON))
		waitForAssert ({
			assertThat event, is(notNullValue())
			assertThat event.topic, is(equalTo("smarthome/items/myLampItem/command"))
		}
		, 5000, 100)
		assertThat event.topic, is(equalTo("smarthome/items/myLampItem/command"))
		assertThat (((ItemCommandEvent)event).itemCommand, is(OnOffType.ON))
	}


	@Test
	public void testRuleApi() {
		//Creation of RULE
		def AutomationFactory automationFactory = getService(AutomationFactory)
		assertThat automationFactory, is(notNullValue())
		def triggerConfig = [itemName:"myMotionItem2"]
		def condition1Config = [operator:"=", itemName:"myPresenceItem2", state:"ON"]
		def condition2Config = [operator:"=", itemName:"myMotionItem2", state:"ON"]
		def actionConfig = [itemName:"myLampItem2", command:"ON"]
		def triggers = [
			automationFactory.createTrigger("ItemStateChangeTrigger2", "ItemStateChangeTrigger", triggerConfig)
		]
		def conditions = [
			automationFactory.createCondition("ItemStateCondition3", "ItemStateCondition", condition1Config, null),
			automationFactory.createCondition("ItemStateCondition4", "ItemStateCondition", condition2Config, null)
		]
		def actions = [
			automationFactory.createAction("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)
		]

		def rule = automationFactory.createRule(triggers, conditions, actions, null, null)
		// I would expect the factory to create the UID of the rule and the name to be in the list of parameters.
		rule.UID="myRule21"
		rule.name="RuleByJAVA_API"

		logger.info("Rule created: "+rule.getUID())

		def ruleRegistry = getService(RuleRegistry)
		ruleRegistry.add(rule)
		ruleRegistry.setEnabled(rule.UID, true)

		//TEST RULE

		def EventPublisher eventPublisher = getService(EventPublisher)
		def ItemRegistry itemRegistry = getService(ItemRegistry)
		SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem2")
		Command commandObj = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")
		eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem2", commandObj))
		//		eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem2", OnOffType.ON))

		Event event = null

		def eventHandler = [
			receive: {  Event e ->
				logger.info("Event: " + e.topic)
				if (e.topic.contains("myLampItem2")){
					event=e
				}
			},

			getSubscribedEventTypes: {
				Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
			},

			getEventFilter:{ null }

		] as EventSubscriber

		registerService(eventHandler)
		commandObj = TypeParser.parseCommand(itemRegistry.getItem("myMotionItem2").getAcceptedCommandTypes(),"ON")
		eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem2", commandObj))
		//		eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem2", OnOffType.ON))
		waitForAssert ({ assertThat event, is(notNullValue())} , 3000, 100)
		assertThat event.topic, is(equalTo("smarthome/items/myLampItem2/state"))
		assertThat (((ItemStateEvent)event).itemState, is(OnOffType.ON))
		def myLampItem2 = itemRegistry.getItem("myLampItem2")
		assertThat myLampItem2, is(notNullValue())
		logger.info("myLampItem2 State: " + myLampItem2.state)
		assertThat myLampItem2.state, is(OnOffType.ON)
	}
}