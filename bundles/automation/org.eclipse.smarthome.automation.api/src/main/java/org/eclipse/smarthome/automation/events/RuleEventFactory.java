/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import java.util.List;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * this is a factory to create Rule Events
 * 
 * @author Benedikt Niehues - initial contribution
 *
 */
public class RuleEventFactory extends AbstractEventFactory {

    private final Logger logger = LoggerFactory.getLogger(RuleEventFactory.class);

    private static final String RULE_STATE_EVENT_TOPIC = "smarthome/rules/{ruleID}/state";

    private static final String RULE_ADDED_EVENT_TOPIC = "smarthome/rules/{ruleID}/added";

    private static final String RULE_REMOVED_EVENT_TOPIC = "smarthome/rules/{ruleID}/removed";

    private static final String RULE_UPDATED_EVENT_TOPIC = "smarthome/rules/{ruleID}/updated";

    public RuleEventFactory() {
        super(Sets.newHashSet(RuleAddedEvent.TYPE, RuleRemovedEvent.TYPE, RuleStatusInfoEvent.TYPE,
                RuleUpdatedEvent.TYPE));
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        logger.debug("creating ruleEvent by Type: {}", eventType);
        if (eventType == null) {
            return null;
        }
        if (eventType.equals(RuleAddedEvent.TYPE)) {
            return createRuleAddedEvent(topic, payload, source);
        } else if (eventType.equals(RuleRemovedEvent.TYPE)) {
            return createRuleRemovedEvent(topic, payload, source);
        } else if (eventType.equals(RuleStatusInfoEvent.TYPE)) {
            return createRuleStatusInfoEvent(topic, payload, source);
        } else if (eventType.equals(RuleUpdatedEvent.TYPE)) {
            return createRuleUpdatedEvent(topic, payload, source);
        }
        return null;
    }

    private Event createRuleUpdatedEvent(String topic, String payload, String source) {
        Rule[] ruleDTO = deserializePayload(payload, Rule[].class);
        if (ruleDTO.length != 2) {
            throw new IllegalArgumentException("Creation of RuleUpdatedEvent failed: invalid payload. " + payload);
        }
        return new RuleUpdatedEvent(topic, payload, source, ruleDTO[0], ruleDTO[1]);
    }

    private Event createRuleStatusInfoEvent(String topic, String payload, String source) {
        RuleStatusInfo statusInfo = deserializePayload(payload, RuleStatusInfo.class);
        return new RuleStatusInfoEvent(topic, payload, source, statusInfo, getRuleId(topic));
    }

    private Event createRuleRemovedEvent(String topic, String payload, String source) {
        Rule ruleDTO = deserializePayload(payload, Rule.class);
        return new RuleRemovedEvent(topic, payload, source, ruleDTO);
    }

    private Event createRuleAddedEvent(String topic, String payload, String source) {
        Rule ruleDTO = deserializePayload(payload, Rule.class);
        return new RuleAddedEvent(topic, payload, source, ruleDTO);
    }

    private String getRuleId(String topic) {
        String[] topicElements = getTopicElements(topic);
        if (topicElements.length != 4)
            throw new IllegalArgumentException("Event creation failed, invalid topic: " + topic);
        return topicElements[2];
    }

    /**
     * creates a rule updated event
     * 
     * @param rule the updated rule
     * @param oldRule the old rule
     * @param source
     * @return
     */
    public static RuleUpdatedEvent createRuleUpdatedEvent(Rule rule, Rule oldRule, String source) {
        String topic = buildTopic(RULE_UPDATED_EVENT_TOPIC, rule);
        List<Rule> rules = Lists.newLinkedList();
        rules.add(rule);
        rules.add(oldRule);
        String payload = serializePayload(rules);
        return new RuleUpdatedEvent(topic, payload, source, rule, oldRule);
    }

    /**
     * creates a rule status info event
     * 
     * @param statusInfo
     * @param rule
     * @param source
     * @return
     */
    public static RuleStatusInfoEvent createRuleStatusInfoEvent(RuleStatusInfo statusInfo, String ruleUID,
            String source) {
        String topic = buildTopic(RULE_STATE_EVENT_TOPIC, ruleUID);
        String payload = serializePayload(statusInfo);
        return new RuleStatusInfoEvent(topic, payload, source, statusInfo, ruleUID);
    }

    /**
     * creates a rule removed event
     * 
     * @param rule
     * @param source
     * @return
     */
    public static RuleRemovedEvent createRuleRemovedEvent(Rule rule, String source) {
        String topic = buildTopic(RULE_REMOVED_EVENT_TOPIC, rule);
        String payload = serializePayload(rule);
        return new RuleRemovedEvent(topic, payload, source, rule);
    }

    /**
     * creates a rule added event
     * 
     * @param rule
     * @param source
     * @return
     */
    public static RuleAddedEvent createRuleAddedEvent(Rule rule, String source) {
        String topic = buildTopic(RULE_ADDED_EVENT_TOPIC, rule);
        String payload = serializePayload(rule);
        return new RuleAddedEvent(topic, payload, source, rule);
    }

    private static String buildTopic(String topic, String ruleUID) {
        return topic.replace("{ruleID}", ruleUID);
    }

    private static String buildTopic(String topic, Rule rule) {
        return buildTopic(topic, rule.getUID());
    }
}
