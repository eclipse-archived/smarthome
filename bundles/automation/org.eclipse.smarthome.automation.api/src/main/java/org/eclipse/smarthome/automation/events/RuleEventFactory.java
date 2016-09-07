/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.automation.dto.RuleDTOMapper;
import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this is a factory to create Rule Events
 *
 * @author Benedikt Niehues - initial contribution
 * @author Markus Rathgeb - Use the DTO for the Rule representation
 */
public class RuleEventFactory extends AbstractEventFactory {

    private final Logger logger = LoggerFactory.getLogger(RuleEventFactory.class);

    private static final String RULE_STATE_EVENT_TOPIC = "smarthome/rules/{ruleID}/state";

    private static final String RULE_ADDED_EVENT_TOPIC = "smarthome/rules/{ruleID}/added";

    private static final String RULE_REMOVED_EVENT_TOPIC = "smarthome/rules/{ruleID}/removed";

    private static final String RULE_UPDATED_EVENT_TOPIC = "smarthome/rules/{ruleID}/updated";

    private static final Set<String> SUPPORTED_TYPES = new HashSet<String>();

    static {
        SUPPORTED_TYPES.add(RuleAddedEvent.TYPE);
        SUPPORTED_TYPES.add(RuleRemovedEvent.TYPE);
        SUPPORTED_TYPES.add(RuleStatusInfoEvent.TYPE);
        SUPPORTED_TYPES.add(RuleUpdatedEvent.TYPE);
    }

    public RuleEventFactory() {
        super(SUPPORTED_TYPES);
    }

    @Override
    protected Event createEventByType(String eventType, String topic, String payload, String source) throws Exception {
        logger.trace("creating ruleEvent of type: {}", eventType);
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
        RuleDTO[] ruleDTO = deserializePayload(payload, RuleDTO[].class);
        if (ruleDTO.length != 2) {
            throw new IllegalArgumentException("Creation of RuleUpdatedEvent failed: invalid payload: " + payload);
        }
        return new RuleUpdatedEvent(topic, payload, source, ruleDTO[0], ruleDTO[1]);
    }

    private Event createRuleStatusInfoEvent(String topic, String payload, String source) {
        RuleStatusInfo statusInfo = deserializePayload(payload, RuleStatusInfo.class);
        return new RuleStatusInfoEvent(topic, payload, source, statusInfo, getRuleId(topic));
    }

    private Event createRuleRemovedEvent(String topic, String payload, String source) {
        RuleDTO ruleDTO = deserializePayload(payload, RuleDTO.class);
        return new RuleRemovedEvent(topic, payload, source, ruleDTO);
    }

    private Event createRuleAddedEvent(String topic, String payload, String source) {
        RuleDTO ruleDTO = deserializePayload(payload, RuleDTO.class);
        return new RuleAddedEvent(topic, payload, source, ruleDTO);
    }

    private String getRuleId(String topic) {
        String[] topicElements = getTopicElements(topic);
        if (topicElements.length != 4) {
            throw new IllegalArgumentException("Event creation failed, invalid topic: " + topic);
        }
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
        final RuleDTO ruleDto = RuleDTOMapper.map(rule);
        final RuleDTO oldRuleDto = RuleDTOMapper.map(oldRule);
        List<RuleDTO> rules = new LinkedList<RuleDTO>();
        rules.add(ruleDto);
        rules.add(oldRuleDto);
        String payload = serializePayload(rules);
        return new RuleUpdatedEvent(topic, payload, source, ruleDto, oldRuleDto);
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
        final RuleDTO ruleDto = RuleDTOMapper.map(rule);
        String payload = serializePayload(ruleDto);
        return new RuleRemovedEvent(topic, payload, source, ruleDto);
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
        final RuleDTO ruleDto = RuleDTOMapper.map(rule);
        String payload = serializePayload(ruleDto);
        return new RuleAddedEvent(topic, payload, source, ruleDto);
    }

    private static String buildTopic(String topic, String ruleUID) {
        return topic.replace("{ruleID}", ruleUID);
    }

    private static String buildTopic(String topic, Rule rule) {
        return buildTopic(topic, rule.getUID());
    }
}
