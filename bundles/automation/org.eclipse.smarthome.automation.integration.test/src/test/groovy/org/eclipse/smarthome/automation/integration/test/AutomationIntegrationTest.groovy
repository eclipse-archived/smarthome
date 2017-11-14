/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.integration.test


import static org.junit.Assert.*

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.Action
import org.eclipse.smarthome.automation.Condition
import org.eclipse.smarthome.automation.ManagedRuleProvider
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleProvider
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.Visibility
import org.eclipse.smarthome.automation.events.RuleAddedEvent
import org.eclipse.smarthome.automation.events.RuleRemovedEvent
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.events.RuleUpdatedEvent
import org.eclipse.smarthome.automation.module.core.handler.GenericEventTriggerHandler
import org.eclipse.smarthome.automation.template.RuleTemplate
import org.eclipse.smarthome.automation.template.RuleTemplateProvider
import org.eclipse.smarthome.automation.template.Template
import org.eclipse.smarthome.automation.template.TemplateRegistry
import org.eclipse.smarthome.automation.type.ActionType
import org.eclipse.smarthome.automation.type.ModuleTypeProvider
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.automation.type.TriggerType
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemCommandEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.FrameworkUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.collect.Sets

/**
 * this tests the RuleEngine
 * @author Benedikt Niehues - initial contribution
 * @author Marin Mitev - various fixes and extracted JSON parser test to separate file
 *
 */
class AutomationIntegrationTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(AutomationIntegrationTest.class)
    def EventPublisher eventPublisher
    def ItemRegistry itemRegistry
    def RuleRegistry ruleRegistry
    def ManagedRuleProvider managedRuleProvider
    def ModuleTypeRegistry moduleTypeRegistry
    def TemplateRegistry templateRegistry

    @Before
    void before() {
        logger.info('@Before.begin')

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
                    new SwitchItem("myMotionItem3"),
                    new SwitchItem("templ_MotionItem"),
                    new SwitchItem("templ_LampItem"),
                    new SwitchItem("myMotionItem3"),
                    new SwitchItem("myPresenceItem3"),
                    new SwitchItem("myLampItem3"),
                    new SwitchItem("myMotionItem4"),
                    new SwitchItem("myPresenceItem4"),
                    new SwitchItem("myLampItem4"),
                    new SwitchItem("myMotionItem5"),
                    new SwitchItem("myPresenceItem5"),
                    new SwitchItem("myLampItem5"),
                    new SwitchItem("xtempl_MotionItem"),
                    new SwitchItem("xtempl_LampItem")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerVolatileStorageService()

        def StorageService storageService = getService(StorageService)
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        ruleRegistry = getService(RuleRegistry)
        managedRuleProvider = getService(ManagedRuleProvider)
        moduleTypeRegistry = getService(ModuleTypeRegistry)
        templateRegistry = getService(TemplateRegistry)
        waitForAssert ({
            assertThat eventPublisher, is(notNullValue())
            assertThat storageService, is(notNullValue())
            assertThat itemRegistry, is(notNullValue())
            assertThat ruleRegistry, is(notNullValue())
            assertThat moduleTypeRegistry, is(notNullValue())
            assertThat templateRegistry, is(notNullValue())
            assertThat managedRuleProvider, is(notNullValue())
        }, 9000)
        logger.info('@Before.finish')
    }

    @After
    void after() {
        logger.info('@After')
    }

    protected void registerVolatileStorageService() {
        registerService(AutomationIntegrationJsonTest.VOLATILE_STORAGE_SERVICE)
    }

    @Test
    public void 'assert that a rule can be added, updated and removed by the api' () {
        logger.info('assert that a rule can be added, updated and removed by the api')
        def ruleEvent = null

        def ruleEventHandler = [
            receive: { Event e ->
                logger.info("RuleEvent: " + e.topic)
                ruleEvent = e
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleAddedEvent.TYPE, RuleRemovedEvent.TYPE, RuleUpdatedEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(ruleEventHandler)

        //ADD
        def Rule rule = createSimpleRule()
        ruleRegistry.add(rule)
        waitForAssert({
            assertThat ruleEvent, is(notNullValue())
            assertThat ruleEvent, is(instanceOf(RuleAddedEvent))
            def ruleAddedEvent = ruleEvent as RuleAddedEvent
            assertThat ruleAddedEvent.getRule().uid, is(rule.UID)
        })
        def Rule ruleAdded = ruleRegistry.get(rule.UID)
        assertThat ruleAdded, is(notNullValue())
        assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(RuleStatus.IDLE)


        //UPDATE
        ruleEvent = null
        ruleAdded.description="TestDescription"
        def Rule oldRule = ruleRegistry.update(ruleAdded)
        waitForAssert({
            assertThat ruleEvent, is(notNullValue())
            assertThat ruleEvent, is(instanceOf(RuleUpdatedEvent))
            def ruEvent = ruleEvent as RuleUpdatedEvent
            assertThat ruEvent.getRule().uid, is(rule.UID)
            assertThat ruEvent.getOldRule().uid, is(rule.UID)
            assertThat ruEvent.getRule().description, is("TestDescription")
            assertThat ruEvent.getOldRule().description, is(nullValue())
        })
        assertThat oldRule, is(notNullValue())
        assertThat oldRule, is(rule)

        //REMOVE
        ruleEvent = null
        def Rule removed = ruleRegistry.remove(rule.UID)
        waitForAssert({
            assertThat ruleEvent, is(notNullValue())
            assertThat ruleEvent, is(instanceOf(RuleRemovedEvent))
            def reEvent = ruleEvent as RuleRemovedEvent
            assertThat reEvent.getRule().uid, is(removed.UID)
        })
        assertThat removed, is(notNullValue())
        assertThat removed, is(ruleAdded)
        assertThat ruleRegistry.get(removed.UID), is(nullValue())
    }

    @Test
    public void 'assert that a rule with connections is executed' () {
        logger.info('assert that a rule with connections is executed')
        def triggerConfig = new Configuration([eventSource:"myMotionItem3", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def condition1Config = new Configuration([topic:"smarthome/*"])
        def actionConfig = new Configuration([itemName:"myLampItem3", command:"ON"])
        def triggers = [
            new Trigger("ItemStateChangeTrigger", "core.GenericEventTrigger", triggerConfig)
        ]

        def inputs = [topic: "ItemStateChangeTrigger.topic", event:"ItemStateChangeTrigger.event"]

        //def conditionInputs=[topicConnection] as Set
        def conditions = [
            new Condition("EventCondition_2", "core.GenericEventCondition", condition1Config, inputs)
        ]
        def actions = [
            new Action("ItemPostCommandAction2", "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21_ConnectionTest")
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions

        rule.name="RuleByJAVA_API"+new Random().nextInt()

        ruleRegistry.add(rule)

        logger.info("Rule created and added: "+rule.getUID())

        def ruleEvents = [] as List<RuleStatusInfoEvent>

        def ruleEventHandler = [
            receive: {  Event e ->
                logger.info("RuleEvent: " + e.topic)
                ruleEvents.add(e)
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleStatusInfoEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(ruleEventHandler)

        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem3",OnOffType.ON))

        waitForAssert({
            assertThat ruleEvents.find{
                it.statusInfo.status == RuleStatus.RUNNING
            }, is(notNullValue())
        }, 9000, 200)
        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(not(RuleStatus.RUNNING))
        })
    }
    @Test
    public void 'assert that a rule with non existing moduleTypeHandler is added to the ruleRegistry in state UNINITIALIZED' () {
        logger.info('assert that a rule with non existing moduleTypeHandler is added to the ruleRegistry in state UNINITIALIZED')
        def triggerConfig = new Configuration([eventSource:"myMotionItem", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def condition1Config = new Configuration([topic:"smarthome/*"])
        def actionConfig = new Configuration([itemName:"myLampItem3", command:"ON"])
        def triggers = [
            new Trigger("ItemStateChangeTrigger", "GenericEventTriggerWhichDoesNotExist", triggerConfig)
        ]
        def inputs = [topic: "ItemStateChangeTrigger.topic", event:"ItemStateChangeTrigger.event"]

        //def conditionInputs=[topicConnection] as Set
        def conditions = [
            new Condition("EventCondition_2", "core.GenericEventCondition", condition1Config, inputs)
        ]
        def actions = [
            new Action("ItemPostCommandAction2", "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21_UNINITIALIZED")
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions

        rule.name="RuleByJAVA_API"+new Random().nextInt()

        ruleRegistry.add(rule)

        assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(RuleStatus.UNINITIALIZED)
    }

    @Test
    public void 'assert that a rule switches from IDLE to UNINITIALIZED if a moduleHanlder disappears and back to IDLE if it appears again' (){
        logger.info('assert that a rule switches from IDLE to UNINITIALIZED if a moduleHanlder disappears and back to IDLE if it appears again')
        def Rule rule = createSimpleRule()
        ruleRegistry.add(rule)
        assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(RuleStatus.IDLE)

        def moduleBundle = FrameworkUtil.getBundle(GenericEventTriggerHandler)
        moduleBundle.stop()
        waitForAssert({
            logger.info("RuleStatus: {}", ruleRegistry.getStatusInfo(rule.UID).getStatus())
            assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(RuleStatus.UNINITIALIZED)
        },3000,100)


        moduleBundle.start()
        ruleRegistry.setEnabled(rule.UID,true)
        waitForAssert({
            logger.info("RuleStatus: {}", ruleRegistry.getStatusInfo(rule.UID))
            assertThat ruleRegistry.getStatusInfo(rule.UID).getStatus(), is(RuleStatus.IDLE)
        },3000,100)
    }

    @Test
    public void 'assert that a module types and templates are disappeared when the providers was uninstalled' (){
        logger.info('assert that a module types and templates are disappeared when the providers was uninstalled')

        waitForAssert({
            logger.info("RuleStatus: {}", moduleTypeRegistry.get('SampleTrigger'))
            assertThat moduleTypeRegistry.get('SampleTrigger'), is(notNullValue())
            assertThat moduleTypeRegistry.get('SampleCondition'), is(notNullValue())
            assertThat moduleTypeRegistry.get('SampleAction'), is(notNullValue())
            assertThat templateRegistry.get('SampleRuleTemplate'), is(notNullValue())
        },3000,100)

        bundleContext.bundles.find {
            if(it.symbolicName == "org.eclipse.smarthome.automation.sample.extension.json") {
                it.uninstall()
                return true
            }
        }

        waitForAssert({
            logger.info("RuleStatus: {}", moduleTypeRegistry.get('SampleTrigger'))
            assertThat moduleTypeRegistry.get('SampleTrigger'), is(nullValue())
            assertThat moduleTypeRegistry.get('SampleCondition'), is(nullValue())
            assertThat moduleTypeRegistry.get('SampleAction'), is(nullValue())
            assertThat templateRegistry.get('SampleRuleTemplate'), is(nullValue())
        },3000,100)
    }


    @Test
    public void 'assert that a rule based on a composite modules is initialized and executed correctly' () {
        def triggerConfig = new Configuration([itemName:"myMotionItem3"])
        def condition1Config = new Configuration([itemName:"myMotionItem3", state:"ON"])
        def eventInputs = [event:"ItemStateChangeTrigger3.event"]
        def condition2Config = new Configuration([operator:"=", itemName:"myPresenceItem3", state:"ON"])
        def actionConfig = new Configuration([itemName:"myLampItem3", command:"ON"])
        def triggers = [
            new Trigger("ItemStateChangeTrigger3", "core.ItemStateChangeTrigger", triggerConfig)
        ]
        def actions = [
            new Action("ItemPostCommandAction3", "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21"+new Random().nextInt()+ "_COMPOSITE")
        rule.triggers = triggers
        rule.actions = actions
        rule.name="RuleByJAVA_API_WIthCompositeTrigger"

        logger.info("Rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry)
        ruleRegistry.add(rule)

        //TEST RULE
        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.uid).getStatus(), is(RuleStatus.IDLE)
        })

        def EventPublisher eventPublisher = getService(EventPublisher)
        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem3", OnOffType.ON))

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem3")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem3", OnOffType.ON))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem3/command"))
        assertThat (((ItemCommandEvent)itemEvent).itemCommand, is(OnOffType.ON))
    }


    @Test
    public void 'assert that ruleNow method executes actions of the rule' () {
        def triggerConfig = new Configuration([eventTopic:"runNowEventTopic/*"])
        def actionConfig = new Configuration([itemName:"myLampItem3", command:"TOGGLE"])
        def actionConfig2 = new Configuration([itemName:"myLampItem3", command:"ON"])
        def actionConfig3 = new Configuration([itemName:"myLampItem3", command:"OFFF"])
        def triggers = [
            new Trigger("GenericEventTriggerId", "core.GenericEventTrigger", triggerConfig)
        ]
        def actions = [
            new Action("ItemPostCommandActionId", "core.ItemCommandAction", actionConfig, null),
            new Action("ItemPostCommandActionId2", "core.ItemCommandAction", actionConfig2, null),
            new Action("ItemPostCommandActionId3", "core.ItemCommandAction", actionConfig3, null)
        ]

        def rule = new Rule("runNowRule"+new Random().nextInt())
        rule.triggers = triggers
        rule.actions = actions
        logger.info("Rule created: "+rule.getUID())

        ruleRegistry.add(rule)

        //TEST RULE
        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.getUID()).getStatus(), is(RuleStatus.IDLE)
        }, 3000, 100)

        def myLampItem3 = itemRegistry.getItem("myLampItem3")
        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem3")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)

        ruleRegistry.runNow(rule.getUID());
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        waitForAssert ({ assertThat itemEvent.itemCommand, is(OnOffType.ON)} , 3000, 100)

        ruleRegistry.remove(rule.getUID())
    }


    @Test
    public void 'test chain of composite Modules' () {
        def triggerConfig = new Configuration([itemName:"myMotionItem4"])
        def eventInputs = [event:"ItemStateChangeTrigger4.event"]
        def actionConfig = new Configuration([itemName:"myLampItem4", command:"ON"])
        def triggers = [
            new Trigger("ItemStateChangeTrigger4", "core.ItemStateChangeTrigger", triggerConfig)
        ]
        def actions = [
            new Action("ItemPostCommandAction4", "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21"+new Random().nextInt()+ "_COMPOSITE")
        rule.triggers = triggers
        rule.actions = actions
        rule.name="RuleByJAVA_API_ChainedComposite"

        logger.info("Rule created: "+rule.getUID())

        def ruleRegistry = getService(RuleRegistry)
        ruleRegistry.add(rule)

        //TEST RULE
        waitForAssert({
            assertThat ruleRegistry.getStatusInfo(rule.uid).getStatus(), is(RuleStatus.IDLE)
        })

        def EventPublisher eventPublisher = getService(EventPublisher)
        SwitchItem myPresenceItem = itemRegistry.getItem("myPresenceItem4")
        //prepare the presenceItems state to be on to match the second condition of the rule
        eventPublisher.post(ItemEventFactory.createStateEvent("myPresenceItem4", OnOffType.ON))

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem4")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        //causing the event to trigger the rule
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem4", OnOffType.ON))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 5000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem4/command"))
        assertThat (((ItemCommandEvent)itemEvent).itemCommand, is(OnOffType.ON))
    }

    @Test
    public void 'assert a rule added by api is executed as expected'() {
        logger.info('assert a rule added by api is executed as expected')
        //Creation of RULE
        def triggerConfig = new Configuration([eventSource:"myMotionItem2", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def actionConfig = new Configuration([itemName:"myLampItem2", command:"ON"])
        def triggers = [
            new Trigger("ItemStateChangeTrigger2", "core.GenericEventTrigger", triggerConfig)
        ]
        def actions = [
            new Action("ItemPostCommandAction2", "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule21")
        rule.triggers = triggers
        rule.actions = actions

        rule.name="RuleByJAVA_API"
        def tags = ["myRule21"] as Set
        rule.tags = tags

        logger.info("Rule created: "+rule.getUID())

        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)
        ruleRegistry.remove(rule.UID)
    }

    @Test
    public void 'assert that a rule can be added by a ruleProvider' () {
        logger.info('assert that a rule can be added by a ruleProvider')
        def rule = createSimpleRule()
        def ruleProvider = [
            getAll:{ [rule]},
            addProviderChangeListener:{},
            removeProviderChangeListener:{
            }
        ] as RuleProvider

        registerService(ruleProvider)
        assertThat ruleRegistry.getAll().find{it.UID==rule.UID}, is(notNullValue())
        unregisterService(ruleProvider)
        assertThat ruleRegistry.getAll().find{it.UID==rule.UID}, is(nullValue())

        def rule2 = createSimpleRule()
        assertThat ruleRegistry.getAll().find{it.UID==rule2.UID}, is(nullValue())
        managedRuleProvider.add(rule2)
        assertThat ruleRegistry.getAll().find{it.UID==rule2.UID}, is(notNullValue())
        managedRuleProvider.remove(rule2.UID)
        assertThat ruleRegistry.getAll().find{it.UID==rule2.UID}, is(nullValue())
    }

    @Test
    public void 'assert that a rule created from a template is executed as expected' () {
        logger.info('assert that a rule created from a template is executed as expected')
        def templateRegistry = getService(TemplateRegistry)
        assertThat templateRegistry, is(notNullValue())
        def template = null
        waitForAssert({
            template = templateRegistry.get("SimpleTestTemplate") as Template
            assertThat template, is(notNullValue())
        })
        assertThat template.tags, is(notNullValue())
        assertThat template.tags.size(), is(not(0))
        def configs = [onItem:"templ_MotionItem", ifState: "ON", updateItem:"templ_LampItem", updateCommand:"ON"]
        def templateRule = new Rule("templateRuleUID")
        templateRule.templateUID = "SimpleTestTemplate"
        templateRule.configuration = configs
        ruleRegistry.add(templateRule)
        assertThat ruleRegistry.getAll().find{it.UID==templateRule.UID}, is(notNullValue())

        Event itemEvent = null
        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("templ_LampItem")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(itemEventHandler)

        //causing the event to trigger the rule
        eventPublisher.post(ItemEventFactory.createStateEvent("templ_MotionItem", OnOffType.ON))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())})
        assertThat itemEvent.topic, is(equalTo("smarthome/items/templ_LampItem/command"))
        assertThat (((ItemCommandEvent)itemEvent).itemCommand, is(OnOffType.ON))
    }

    @Test
    public void 'assert that a rule created from a more complex template is executed as expected' () {
        logger.info('assert that a rule created from a more complex template is executed as expected')
        def templateRegistry = getService(TemplateRegistry)
        assertThat templateRegistry, is(notNullValue())
        def template = null
        waitForAssert({
            template = templateRegistry.get("TestTemplateWithCompositeModules") as Template
            assertThat template, is(notNullValue())
        })
        assertThat template.tags, is(notNullValue())
        assertThat template.tags.size(), is(not(0))

        def configs = new Configuration([onItem:"xtempl_MotionItem", ifState: ".*ON.*", updateItem:"xtempl_LampItem", updateCommand:"ON"])
        def templateRule = new Rule("xtemplateRuleUID")
        templateRule.templateUID = "TestTemplateWithCompositeModules"
        templateRule.configuration = configs

        ruleRegistry.add(templateRule)
        assertThat ruleRegistry.getAll().find{it.UID==templateRule.UID}, is(notNullValue())
        waitForAssert {
            assertThat ruleRegistry.get(templateRule.UID), is(notNullValue())
            assertThat ruleRegistry.getStatus(templateRule.UID), is(RuleStatus.IDLE)
        }

        Event itemEvent = null
        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("xtempl_LampItem")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }
        ] as EventSubscriber
        registerService(itemEventHandler)

        //bring the rule to execution:
        eventPublisher.post(ItemEventFactory.createStateEvent("xtempl_MotionItem", OnOffType.ON))

        waitForAssert ({ assertThat itemEvent, is(notNullValue())})
        assertThat itemEvent.topic, is(equalTo("smarthome/items/xtempl_LampItem/command"))
        assertThat (((ItemCommandEvent)itemEvent).itemCommand, is(OnOffType.ON))
    }

    @Test
    public void 'test ModuleTypeProvider and TemplateProvider'(){
        logger.info('test ModuleTypeProvider and TemplateProvider')
        def templateRegistry = getService(TemplateRegistry)
        def moduleTypeRegistry = getService(ModuleTypeRegistry)
        def templateUID = 'testTemplate1'
        def tags = ["test", "testTag"] as Set
        def templateTriggers = []
        def templateConditions = []
        def templateActions = []
        def templateConfigDescriptionParameters = [
            new ConfigDescriptionParameter("param", ConfigDescriptionParameter.Type.TEXT)
        ]
        def template = new RuleTemplate(templateUID, "Test template Label", "Test template description", tags, templateTriggers, templateConditions,
                templateActions, templateConfigDescriptionParameters, Visibility.VISIBLE)

        def triggerTypeUID = "testTrigger1"
        def triggerType = new TriggerType(triggerTypeUID, templateConfigDescriptionParameters, null)
        def actionTypeUID = "testAction1"
        def actionType = new ActionType(actionTypeUID, templateConfigDescriptionParameters, null)

        def templateProvider=[
            getTemplate:{ String UID, Locale locale ->
                if (UID == templateUID){
                    return template
                }else{
                    return null
                }
            },
            getTemplates:{Locale locale->
                return [template]
            },
            getAll:{ return [template]},
            addProviderChangeListener:{ ProviderChangeListener listener ->
            },
            removeProviderChangeListener:{ ProviderChangeListener listener ->
            }
        ] as RuleTemplateProvider

        def moduleTypeProvider=[
            getModuleType:{String UID, Locale locale->
                if (UID==triggerTypeUID){
                    return triggerType
                } else if (UID == actionTypeUID){
                    return actionType
                } else {
                    return null
                }
            },
            getModuleTypes:{Locale locale ->
                return [triggerType, actionType]
            },
            getAll:{ return [triggerType, actionType]},
            addProviderChangeListener:{ ProviderChangeListener listener ->
            },
            removeProviderChangeListener:{ ProviderChangeListener listener ->
            }
        ] as ModuleTypeProvider

        registerService(templateProvider)
        assertThat templateRegistry.get(templateUID), is(notNullValue())
        unregisterService(templateProvider)
        assertThat templateRegistry.get(templateUID), is(nullValue())

        registerService(moduleTypeProvider)
        assertThat moduleTypeRegistry.get(actionTypeUID), is(notNullValue())
        assertThat moduleTypeRegistry.get(triggerTypeUID), is(notNullValue())
        unregisterService(moduleTypeProvider)
        assertThat moduleTypeRegistry.get(actionTypeUID), is(nullValue())
        assertThat moduleTypeRegistry.get(triggerTypeUID), is(nullValue())
    }

    /**
     * creates a simple rule
     */
    private Rule createSimpleRule(){
        logger.info("createSimpleRule")
        def rand = new Random().nextInt()
        def triggerConfig = new Configuration([eventSource:"myMotionItem2", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def actionConfig = new Configuration([itemName:"myLampItem2", command:"ON"])
        def triggerUID = "ItemStateChangeTrigger_"+rand
        def triggers = [
            new Trigger(triggerUID, "core.GenericEventTrigger", triggerConfig)
        ]
        def actions = [
            new Action("ItemPostCommandAction_"+rand, "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule_"+rand)
        rule.triggers = triggers
        rule.actions = actions
        rule.name="RuleByJAVA_API_"+rand

        logger.info("Rule created: "+rule.getUID())
        return rule
    }


    @Test
    public void 'assert a rule with generic condition works'() {
        def random = new Random().nextInt(100000)
        logger.info('assert a rule with generic condition works')
        //Creation of RULE
        def triggerConfig = new Configuration([eventSource:"myMotionItem5", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"])
        def condition1Config = new Configuration([operator:"matches", right:".*ON.*", inputproperty:"payload"])
        def condition2Config = new Configuration([operator:"=", right:"myMotionItem5", inputproperty:"itemName"])
        def actionConfig = new Configuration([itemName:"myLampItem5", command:"ON"])
        def triggerId = "ItemStateChangeTrigger"+random
        def triggers = [
            new Trigger(triggerId, "core.GenericEventTrigger", triggerConfig)
        ]
        def conditions = [
            new Condition("ItemStateCondition"+random, "core.GenericCompareCondition", condition1Config, [input:triggerId+".event"]),
            new Condition("ItemStateCondition"+(random+1), "core.GenericCompareCondition", condition2Config, [input:triggerId+".event"])
        ]
        def actions = [
            new Action("ItemPostCommandAction"+random, "core.ItemCommandAction", actionConfig, null)
        ]

        def rule = new Rule("myRule_"+random)
        rule.triggers = triggers
        rule.conditions = conditions
        rule.actions = actions

        rule.name="RuleByJAVA_API"+random
        def tags = ["myRule_"+random] as Set
        rule.tags = tags

        logger.info("Rule created: "+rule.getUID())

        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        //WAIT until Rule modules types are parsed and the rule becomes IDLE
        waitForAssert({
            assertThat ruleRegistry.getAll().isEmpty(), is(false)
            def rule2 = ruleRegistry.get(rule.UID)
            assertThat rule2, is(notNullValue())
            def ruleStatus2 = ruleRegistry.getStatusInfo(rule2.uid).status as RuleStatus
            assertThat ruleStatus2, is(RuleStatus.IDLE)
        }, 10000, 200)


        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myMotionItem = itemRegistry.getItem("myPresenceItem5")
        myMotionItem.setState(OnOffType.ON)

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem5")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemCommandEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        eventPublisher.post(ItemEventFactory.createStateEvent("myMotionItem5", OnOffType.ON))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem5/command"))
        assertThat (((ItemCommandEvent)itemEvent).itemCommand, is(OnOffType.ON))
    }

}

