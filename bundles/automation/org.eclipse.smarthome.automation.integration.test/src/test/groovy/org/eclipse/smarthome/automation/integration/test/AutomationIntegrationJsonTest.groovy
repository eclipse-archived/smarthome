/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.integration.test;

import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.RuleStatusInfo
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.type.ModuleType
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkEvent
import org.osgi.framework.FrameworkListener
import org.osgi.framework.wiring.FrameworkWiring
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets

/**
 * This tests the RuleEngine and the import from JSON resources contained in the ESH-INF folder.
 * This test must be run first otherwise imported rules will be cleared.
 *
 * @author Benedikt Niehues - initial contribution
 * @author Marin Mitev - make the test to pass on each run
 *
 */
class AutomationIntegrationJsonTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(AutomationIntegrationJsonTest.class)
    def EventPublisher eventPublisher
    def ItemRegistry itemRegistry
    def RuleRegistry ruleRegistry
    def ModuleTypeRegistry moduleTypeRegistry
    def Event ruleEvent
    public static def VolatileStorageService VOLATILE_STORAGE_SERVICE = new VolatileStorageService()//keep storage with rules imported from json files

    @Before
    void before() {
        logger.info('@Before.begin');

        getService(ItemRegistry)
        def itemProvider = [
            getAll: {
                [
                    new SwitchItem("myMotionItem"),
                    new SwitchItem("myPresenceItem"),
                    new SwitchItem("myLampItem"),
                    new SwitchItem("myMotionItem2"),
                    new SwitchItem("myPresenceItem2"),
                    new SwitchItem("myLampItem2"),
                    new SwitchItem("myMotionItem11"),
                    new SwitchItem("myLampItem11"),
                    new SwitchItem("myMotionItem3"),
                    new SwitchItem("templ_MotionItem"),
                    new SwitchItem("templ_LampItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerVolatileStorageService()

        //        enableItemAutoUpdate()
        def autoupdateConfig = [
            autoUpdate: { String itemName ->
                println "AutoUpdate Item -> " + itemName
                return true }

        ] as AutoUpdateBindingConfigProvider
        registerService(autoupdateConfig)

        def ruleEventHandler = [
            receive: { Event e ->
                logger.info("RuleEvent: " + e.topic + " --> " + e.payload)
                println "RuleEvent: " + e.topic + " --> " + e.payload
                if (e.payload.contains("RUNNING")){

                    ruleEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleStatusInfoEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber
        registerService(ruleEventHandler)

        def StorageService storageService = getService(StorageService)
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        ruleRegistry = getService(RuleRegistry)
        moduleTypeRegistry = getService(ModuleTypeRegistry)
        waitForAssert ({
            assertThat storageService, is(notNullValue())

            assertThat eventPublisher, is(notNullValue()) //sometimes assert fails because EventPublisher service is null
            assertThat itemRegistry, is(notNullValue())
            assertThat ruleRegistry, is(notNullValue())
            assertThat moduleTypeRegistry, is(notNullValue())
        }, 9000)
        logger.info('@Before.finish');
    }

    @After
    void after() {
        logger.info('@After');
    }


    protected void registerVolatileStorageService() {
        registerService(VOLATILE_STORAGE_SERVICE);
    }


    @Test
    public void 'assert that a rule from json file is added automatically' () {
        logger.info("assert that a rule from json file is added automatically");

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest")} as Rule
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatus(rule2.uid) as RuleStatusInfo
            assertThat ruleStatus2.getStatus(), is(RuleStatus.IDLE)
        }, 10000, 200)
        def rule = ruleRegistry.getAll().find{it.tags!=null && it.tags.contains("jsonTest")} as Rule
        assertThat rule, is(notNullValue())
        assertThat rule.name, is("ItemSampleRule")
        assertTrue rule.tags.any{it == "sample"}
        assertTrue rule.tags.any{it == "item"}
        assertTrue rule.tags.any{it == "rule"}
        def trigger = rule.triggers.find{it.id.equals("ItemStateChangeTriggerID")} as Trigger
        assertThat trigger, is(notNullValue())
        assertThat trigger.typeUID, is("GenericEventTrigger")
        assertThat trigger.configuration.get("eventSource"), is ("myMotionItem")
        assertThat trigger.configuration.get("eventTopic"), is("smarthome/items/*")
        assertThat trigger.configuration.get("eventTypes"), is("ItemStateEvent")
        def condition1 = rule.conditions.find{it.id.equals("ItemStateConditionID")} as Condition
        assertThat condition1, is(notNullValue())
        assertThat condition1.typeUID, is("EventCondition")
        assertThat condition1.configuration.get("topic"), is("smarthome/items/myMotionItem/state")
        assertThat condition1.configuration.get("payload"), is(".*ON.*")
        def action = rule.actions.find{it.id.equals("ItemPostCommandActionID")} as Action
        assertThat action, is(notNullValue())
        assertThat action.typeUID, is("ItemPostCommandAction")
        assertThat action.configuration.get("itemName"), is("myLampItem")
        assertThat action.configuration.get("command"), is("ON")
        def ruleStatus = ruleRegistry.getStatus(rule.uid) as RuleStatusInfo
        assertThat ruleStatus.getStatus(), is(RuleStatus.IDLE)
    }


    @Test
    public void 'assert that a rule from json file is executed correctly' () {
        logger.info('assert that rule added by json is executed correctly');
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            Rule r = ruleRegistry.get("ItemSampleRule")
            assertThat r, is(notNullValue())
            assertThat ruleRegistry.getStatus(r.UID).getStatus(), is(RuleStatus.IDLE)

        }, 3000, 200)
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem", OnOffType.ON))
        SwitchItem myLampItem = itemRegistry.getItem("myLampItem")
        assertThat myLampItem.getState(), is(UnDefType.NULL)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem")
        Event event = null
        def eventHandler = [
            receive: { Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic == "smarthome/items/myLampItem/state"){
                    event=e
                }
            },
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemStateEvent.TYPE)
            },
            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(eventHandler)
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem", OnOffType.ON))
        waitForAssert ({
            assertThat (myLampItem.getState(), is(OnOffType.ON))
            assertThat event, is(notNullValue())
            assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        }, 9000, 100)

        assertThat event.topic, is(equalTo("smarthome/items/myLampItem/state"))
        assertThat (((ItemStateEvent)event).itemState, is(OnOffType.ON))


    }

    @Test
    public void 'asserting that the host-fragment support works correctly' () {
        logger.info('asserting that the host-fragment support works correctly')

        // first install the host
        Bundle testAutomationProviderHost = bundleContext.installBundle('automation.test.host', getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.host.jar"))
        testAutomationProviderHost.start()
        assertThat testAutomationProviderHost.getState(), is(Bundle.ACTIVE)

        // assert that the host resources are loaded
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            Rule r = ruleRegistry.get("AutomationTestHostRule")
            assertThat r, is(notNullValue())
            ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(notNullValue())
        }, 3000, 200)

        // then install the fragment
        Bundle testAutomationProviderFragmet = bundleContext.installBundle('automation.test.fragment', getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.fragment.jar"))
        assertThat testAutomationProviderFragmet.getState(), is(Bundle.RESOLVED)

        // assert that the host and fragment resources are loaded
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            Rule r = ruleRegistry.get("AutomationTestFragmentRule")
            assertThat r, is(notNullValue())
            ModuleType m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(notNullValue())
            m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(notNullValue())
        }, 3000, 200)

        // first uninstall the host
        testAutomationProviderHost.uninstall()
        assertThat testAutomationProviderHost.getState(), is(Bundle.UNINSTALLED)

        // assert that the host and fragment resources are removed
        waitForAssert({
            ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(nullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(nullValue())
        }, 3000, 200)

        // uninstall the fragment
        testAutomationProviderFragmet.uninstall()
        assertThat testAutomationProviderFragmet.getState(), is(Bundle.UNINSTALLED)

        // first install the fragment
        testAutomationProviderFragmet = bundleContext.installBundle('automation.test.fragment', getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.fragment.jar"))
        assertThat testAutomationProviderFragmet.getState(), is(Bundle.INSTALLED)

        // assert that the host and fragment resources are not loaded
        waitForAssert({
            ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(nullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(nullValue())
        }, 3000, 200)

        // then install the host
        testAutomationProviderHost = bundleContext.installBundle('automation.test.host', getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.host.jar"))
        testAutomationProviderHost.start()

        // assert that the host and fragment resources are loaded
        waitForAssert({
            assertThat testAutomationProviderHost.getState(), is(Bundle.ACTIVE)
            assertThat testAutomationProviderFragmet.getState(), is(Bundle.RESOLVED)
            ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(notNullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(notNullValue())
        }, 3000, 200)

        testAutomationProviderFragmet.update(getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.fragment_updated.jar"))
        Bundle systemBundle = bundleContext.getBundle(0)
        FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class)

        def waiting = true
        def bundles = [testAutomationProviderHost, testAutomationProviderFragmet]
        def FrameworkListener listener = new FrameworkListener() {
                    public void frameworkEvent(FrameworkEvent event) {
                        waiting = false
                        assertThat testAutomationProviderHost.getState(), is(Bundle.ACTIVE)
                        assertThat testAutomationProviderFragmet.getState(), is(Bundle.RESOLVED)
                        ModuleType m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
                        assertThat m, is(notNullValue())
                        m = moduleTypeRegistry.get("AutomationTestFragmentAction")
                        assertThat m, is(notNullValue())
                        m = moduleTypeRegistry.get("AutomationTestHostAction")
                        assertThat m, is(notNullValue())
                    }
                }
        frameworkWiring.refreshBundles(bundles, listener)
        while ({
            sleep(3000)
            waiting == true
        }()) continue

            testAutomationProviderHost.update(getClass().getClassLoader().getResourceAsStream("src/test/resources/automation.test.host_updated.jar"))

        waitForAssert({
            assertThat testAutomationProviderHost.getState(), is(Bundle.ACTIVE)
            assertThat testAutomationProviderFragmet.getState(), is(Bundle.RESOLVED)
            ModuleType m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(notNullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentAction")
            assertThat m, is(notNullValue())
            m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(notNullValue())
            m = moduleTypeRegistry.get("AutomationTestHostTrigger")
            assertThat m, is(notNullValue())
        }, 3000, 200)

        // first uninstall the fragment
        testAutomationProviderFragmet.uninstall()
        assertThat testAutomationProviderFragmet.getState(), is(Bundle.UNINSTALLED)
        waiting = true
        listener = new FrameworkListener() {
                    public void frameworkEvent(FrameworkEvent event) {
                        waiting = false
                        // assert that the host is updated and only its resources are available
                        ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
                        assertThat m, is(notNullValue())
                        m = moduleTypeRegistry.get("AutomationTestHostTrigger")
                        assertThat m, is(notNullValue())
                        m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
                        assertThat m, is(nullValue())
                        m = moduleTypeRegistry.get("AutomationTestFragmentAction")
                        assertThat m, is(nullValue())
                    }
                }
        frameworkWiring.refreshBundles(bundles, listener)
        while ({
            sleep(3000)
            waiting == true
        }()) continue

            // then uninstall the host
            testAutomationProviderHost.uninstall()
        assertThat testAutomationProviderHost.getState(), is(Bundle.UNINSTALLED)

        // assert that the host resources also are removed
        waitForAssert({
            ModuleType m = moduleTypeRegistry.get("AutomationTestHostAction")
            assertThat m, is(nullValue())
            m = moduleTypeRegistry.get("AutomationTestHostTrigger")
            assertThat m, is(nullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentTrigger")
            assertThat m, is(nullValue())
            m = moduleTypeRegistry.get("AutomationTestFragmentAction")
            assertThat m, is(nullValue())
        }, 3000, 200)

    }

}