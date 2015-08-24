package org.eclipse.smarthome.automation.events;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.core.events.AbstractEventFactory;
import org.eclipse.smarthome.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class RuleEventFactory extends AbstractEventFactory {

	private static Logger logger = LoggerFactory.getLogger(RuleEventFactory.class);

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
			throw new IllegalArgumentException("Creation of RuleUpdatedEvent failed: invalid payload. " + payload);
		}
		return new RuleUpdatedEvent(topic, payload, source, ruleDTO[0], ruleDTO[1]);
	}

	private Event createRuleStatusInfoEvent(String topic, String payload, String source) {
		// TODO hanlde StatusInfoEvent!!
		return new RuleStatusInfoEvent(topic, payload, source, null, getRuleId(topic));
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
		if (topicElements.length != 4)
			throw new IllegalArgumentException("Event creation failed, invalid topic: " + topic);
		return topicElements[2];
	}

	private RuleDTO map(Rule rule) {

		return null;
	}

}
