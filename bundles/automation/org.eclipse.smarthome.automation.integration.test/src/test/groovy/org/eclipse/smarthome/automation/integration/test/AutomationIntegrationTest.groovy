/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.automation.Connection
import org.eclipse.smarthome.automation.Rule
import org.eclipse.smarthome.automation.RuleProvider
import org.eclipse.smarthome.automation.RuleRegistry
import org.eclipse.smarthome.automation.RuleStatus
import org.eclipse.smarthome.automation.Trigger
import org.eclipse.smarthome.automation.events.RuleAddedEvent
import org.eclipse.smarthome.automation.events.RuleRemovedEvent
import org.eclipse.smarthome.automation.events.RuleStatusInfoEvent
import org.eclipse.smarthome.automation.events.RuleUpdatedEvent
import org.eclipse.smarthome.automation.module.core.handler.GenericEventTriggerHandler
import org.eclipse.smarthome.automation.template.RuleTemplate
import org.eclipse.smarthome.automation.template.Template
import org.eclipse.smarthome.automation.template.TemplateProvider
import org.eclipse.smarthome.automation.template.TemplateRegistry
import org.eclipse.smarthome.automation.template.Template.Visibility
import org.eclipse.smarthome.automation.type.ActionType
import org.eclipse.smarthome.automation.type.ModuleTypeProvider
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.automation.type.TriggerType
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.TypeParser
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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

    @Before
    void before() {
        logger.info('@Before.begin');

        getService(ItemRegistry)
        def itemProvider = [
            getAll: {
                [new SwitchItem("myMotionItem"), new SwitchItem("myPresenceItem"), new SwitchItem("myLampItem"), new SwitchItem("myMotionItem2"), new SwitchItem("myPresenceItem2"), new SwitchItem("myLampItem2"), new SwitchItem("myMotionItem3"), new SwitchItem("templ_MotionItem"), new SwitchItem("templ_LampItem")]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider
        registerService(itemProvider)
        registerVolatileStorageService()

        enableItemAutoUpdate()

        def StorageService storageService = getService(StorageService)
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        ruleRegistry = getService(RuleRegistry)
        waitForAssert ({
            assertThat eventPublisher, is(notNullValue())
            assertThat storageService, is(notNullValue())
            assertThat itemRegistry, is(notNullValue())
            assertThat ruleRegistry, is(notNullValue())
        }, 9000)
        logger.info('@Before.finish');
    }

    @After
    void after() {
        logger.info('@After');
    }

    @Test
    public void 'assert that a rule can be added, updated and removed by the api' () {
        logger.info('assert that a rule can be added, updated and removed by the api');
        def ruleEvent = null

        def ruleEventHandler = [
            receive: {  Event e ->
                logger.info("RuleEvent: " + e.topic)
                ruleEvent=e
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(RuleAddedEvent.TYPE, RuleRemovedEvent.TYPE, RuleStatusInfoEvent.TYPE, RuleUpdatedEvent.TYPE)
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
            assertThat ruleAddedEvent.getRule().UID, is(rule.UID)
        })
        def Rule ruleAdded = ruleRegistry.get(rule.UID)
        assertThat ruleAdded, is(notNullValue())
        assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.IDLE)


        //UPDATE
        ruleEvent = null
        ruleAdded.description="TestDescription"
        def Rule oldRule = ruleRegistry.update(ruleAdded)
        waitForAssert({
            assertThat ruleEvent, is(notNullValue())
            assertThat ruleEvent, is(instanceOf(RuleUpdatedEvent))
            def ruEvent = ruleEvent as RuleUpdatedEvent
            assertThat ruEvent.getRule().UID, is(rule.UID)
            assertThat ruEvent.getOldRule().UID, is(rule.UID)
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
            assertThat reEvent.getRule().UID, is(removed.UID)
        })
        assertThat removed, is(notNullValue())
        assertThat removed, is(ruleAdded)
        assertThat ruleRegistry.get(removed.UID), is(nullValue())
    }

    @Test
    public void 'assert that a rule with connections is executed' () {
        logger.info('assert that a rule with connections is executed');
        def triggerConfig = [eventSource:"myMotionItem3", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"]
        def condition1Config = [topic:"smarthome/*"]
        def actionConfig = [itemName:"myLampItem3", command:"ON"]
        def triggers = [new Trigger("ItemStateChangeTrigger", "GenericEventTrigger", triggerConfig)]
        Connection topicConnection = new Connection("topic", "ItemStateChangeTrigger", "topic")

        def conditionInputs=[topicConnection] as Set
        def conditions = [new Condition("EventCondition_2", "EventCondition", condition1Config, conditionInputs)]
        def actions = [new Action("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule21_ConnectionTest",triggers, conditions, actions, null, null)
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
            assertThat ruleRegistry.getStatus(rule.UID), is(not(RuleStatus.RUNNING))
        })
    }
    @Test
    public void 'assert that a rule with non existing moduleTypeHandler is added to the ruleRegistry in state NOT_INITIALIZED' () {
        logger.info('assert that a rule with non existing moduleTypeHandler is added to the ruleRegistry in state NOT_INITIALIZED');
        def triggerConfig = [eventSource:"myMotionItem", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"]
        def condition1Config = [topic:"smarthome/*"]
        def actionConfig = [itemName:"myLampItem3", command:"ON"]
        def triggers = [new Trigger("ItemStateChangeTrigger", "GenericEventTriggerWhichDoesNotExist", triggerConfig)]
        Connection topicConnection = new Connection("topic", "ItemStateChangeTrigger", "topic")

        def conditionInputs=[topicConnection] as Set
        def conditions = [new Condition("EventCondition_2", "EventCondition", condition1Config, conditionInputs)]
        def actions = [new Action("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule21_UNINITIALIZED",triggers, conditions, actions, null, null)
        rule.name="RuleByJAVA_API"+new Random().nextInt()

        ruleRegistry.add(rule)

        assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.NOT_INITIALIZED)
    }

    @Test
    public void 'assert that a rule switches from IDLE to NOT_INITIALIZED if a moduleHanlder disappears and back to IDLE if it appears again' (){
        logger.info('assert that a rule switches from IDLE to NOT_INITIALIZED if a moduleHanlder disappears and back to IDLE if it appears again');
        def Rule rule = createSimpleRule()
        ruleRegistry.add(rule)
        assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.IDLE)

        def moduleBundle = FrameworkUtil.getBundle(GenericEventTriggerHandler)
        moduleBundle.stop()
        assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.NOT_INITIALIZED)
        moduleBundle.start()
        ruleRegistry.setEnabled(rule.UID,true)
        waitForAssert({
            logger.info("RuleStatus: {}", ruleRegistry.getStatus(rule.UID))
            assertThat ruleRegistry.getStatus(rule.UID), is(RuleStatus.IDLE)
        },3000,100)
    }

    @Test
    @Ignore
    public void 'assert that a template-based rule is initialized and executed correctly' () {
    }
    @Test
    @Ignore
    public void 'assert that a rule based on a composite trigger is initialized and executed correctly' () {
    }
    @Test
    @Ignore
    public void 'assert that a rule based on a composite condition is initialized and executed correctly' () {
    }
    @Test
    @Ignore
    public void 'assert that a rule based on a composite action is initialized and executed correctly' () {
    }



    @Test
    public void 'assert a rule added by api is executed as expected'() {
        logger.info('assert a rule added by api is executed as expected');
        //Creation of RULE
        def triggerConfig = [eventSource:"myMotionItem2", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"]
        def condition1Config = [operator:"=", itemName:"myPresenceItem2", state:"ON"]
        def condition2Config = [operator:"=", itemName:"myMotionItem2", state:"ON"]
        def actionConfig = [itemName:"myLampItem2", command:"ON"]
        def triggers = [new Trigger("ItemStateChangeTrigger2", "GenericEventTrigger", triggerConfig)]
        def conditions = [new Condition("ItemStateCondition3", "ItemStateCondition", condition1Config, null), new Condition("ItemStateCondition4", "ItemStateCondition", condition2Config, null)]
        def actions = [new Action("ItemPostCommandAction2", "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule21",triggers, conditions, actions, null, null)
        rule.name="RuleByJAVA_API"

        logger.info("Rule created: "+rule.getUID())

        ruleRegistry.add(rule)
        ruleRegistry.setEnabled(rule.UID, true)

        //TEST RULE

        def EventPublisher eventPublisher = getService(EventPublisher)
        def ItemRegistry itemRegistry = getService(ItemRegistry)
        SwitchItem myMotionItem = itemRegistry.getItem("myMotionItem2")
        Command commandObj = TypeParser.parseCommand(myMotionItem.getAcceptedCommandTypes(), "ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myPresenceItem2", commandObj))

        Event itemEvent = null

        def itemEventHandler = [
            receive: {  Event e ->
                logger.info("Event: " + e.topic)
                if (e.topic.contains("myLampItem2")){
                    itemEvent=e
                }
            },

            getSubscribedEventTypes: {
                Sets.newHashSet(ItemUpdatedEvent.TYPE, ItemStateEvent.TYPE)
            },

            getEventFilter:{ null }

        ] as EventSubscriber

        registerService(itemEventHandler)
        commandObj = TypeParser.parseCommand(itemRegistry.getItem("myMotionItem2").getAcceptedCommandTypes(),"ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("myMotionItem2", commandObj))
        waitForAssert ({ assertThat itemEvent, is(notNullValue())} , 3000, 100)
        assertThat itemEvent.topic, is(equalTo("smarthome/items/myLampItem2/state"))
        assertThat (((ItemStateEvent)itemEvent).itemState, is(OnOffType.ON))
        def myLampItem2 = itemRegistry.getItem("myLampItem2")
        assertThat myLampItem2, is(notNullValue())
        logger.info("myLampItem2 State: " + myLampItem2.state)
        assertThat myLampItem2.state, is(OnOffType.ON)
    }

    @Test
    public void 'assert that a rule can be added by a ruleProvider' () {
        logger.info('assert that a rule can be added by a ruleProvider');
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
    }

    @Test
    public void 'assert that a rule created from a template is executed as expected' () {
        logger.info('assert that a rule created from a template is executed as expected');
        def templateRegistry = getService(TemplateRegistry)
        assertThat templateRegistry, is(notNullValue())
        def template = null
        waitForAssert({
            template = templateRegistry.get("SimpleTestTemplate") as Template
            assertThat template, is(notNullValue())
        })
        def configs = [onItem:"templ_MotionItem", ifState: "ON", updateItem:"templ_LampItem", updateCommand:"ON"]
        def templateRule = new Rule("templateRuleUID", "SimpleTestTemplate", configs)
        ruleRegistry.add(templateRule)
        assertThat ruleRegistry.getAll().find{it.UID==templateRule.UID}, is(notNullValue())

        //bring the rule to execution:
        def commandObj = TypeParser.parseCommand(itemRegistry.getItem("templ_MotionItem").getAcceptedCommandTypes(),"ON")
        eventPublisher.post(ItemEventFactory.createCommandEvent("templ_MotionItem", commandObj))

        waitForAssert({
            def lamp = itemRegistry.getItem("templ_LampItem") as SwitchItem
            assertThat lamp.state, is(OnOffType.ON)
        })

    }

    @Test
    public void 'test ModuleTypeProvider and TemplateProvider'(){
        logger.info('test ModuleTypeProvider and TemplateProvider');
        def templateRegistry = getService(TemplateRegistry)
        def moduleTypeRegistry = getService(ModuleTypeRegistry)
        def templateUID = 'testTemplate1'
        def tags = ["test", "testTag"] as Set
        def templateTriggers = []
        def templateConditions = []
        def templateActions = []
        def templateConfigDescriptionParameters = [new ConfigDescriptionParameter("param", Type.TEXT)
        ] as Set

        def template = new RuleTemplate(templateUID, "Test template Label", "Test template description", tags, templateTriggers, templateConditions, templateActions, templateConfigDescriptionParameters, Visibility.PUBLIC)

        def triggerTypeUID = "testTrigger1"
        def triggerType = new TriggerType(triggerTypeUID, templateConfigDescriptionParameters, null)
        def actionTypeUID = "testAction1"
        def actionType = new ActionType(actionTypeUID, templateConfigDescriptionParameters, null)

        def templateProvider=[
            getTemplate:{ String UID, Locale locale ->
                if (UID == templateUID){
                    return template
                }else{
                    return null;
                }
            },

            getTemplates:{Locale locale->
                return [template]
            }
        ] as TemplateProvider

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
            }
        ] as ModuleTypeProvider

        registerService(templateProvider)
        assertThat templateRegistry.get(templateUID), is(notNullValue())
        registerService(moduleTypeProvider)
        assertThat moduleTypeRegistry.get(actionTypeUID), is(notNullValue())
        assertThat moduleTypeRegistry.get(triggerTypeUID), is(notNullValue())

        unregisterService(templateProvider)
        assertThat templateRegistry.get(templateUID), is(nullValue())
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
        def triggerConfig = [eventSource:"myMotionItem2", eventTopic:"smarthome/*", eventTypes:"ItemStateEvent"]
        def condition1Config = [operator:"=", itemName:"myPresenceItem2", state:"ON"]
        def condition2Config = [operator:"=", itemName:"myMotionItem2", state:"ON"]
        def actionConfig = [itemName:"myLampItem2", command:"ON"]
        def triggers = [new Trigger("ItemStateChangeTrigger_"+rand, "GenericEventTrigger", triggerConfig)]
        def conditions = [new Condition("ItemStateCondition_"+rand, "ItemStateCondition", condition1Config, null), new Condition("ItemStateCondition1_"+rand, "ItemStateCondition", condition2Config, null)]
        def actions = [new Action("ItemPostCommandAction_"+rand, "ItemPostCommandAction", actionConfig, null)]

        def rule = new Rule("myRule_"+rand,triggers, conditions, actions, null, null)
        rule.name="RuleByJAVA_API_"+rand

        logger.info("Rule created: "+rule.getUID())
        return rule
    }


}