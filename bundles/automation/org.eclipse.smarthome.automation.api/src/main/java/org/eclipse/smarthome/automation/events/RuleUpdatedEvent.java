/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import org.eclipse.smarthome.automation.dto.RuleDTO;

/**
 * An {@link RuleUpdatedEvent} notifies subscribers that an item has been added.
 * Rule added events must be created with the {@link RuleEventFactory}.
 * 
 * @author Benedikt Niehues - initial contribution
 *
 */
public class RuleUpdatedEvent extends AbstractRuleRegistryEvent {

	public static final String TYPE = RuleUpdatedEvent.class.getSimpleName();

	private RuleDTO oldRuleDTO;

	/**
	 * constructs a new rule updated event
	 * 
	 * @param topic
	 * @param payload
	 * @param source
	 * @param ruleDTO
	 */
	public RuleUpdatedEvent(String topic, String payload, String source, RuleDTO ruleDTO, RuleDTO oldRuleDTO) {
		super(topic, payload, source, ruleDTO);
		this.oldRuleDTO = oldRuleDTO;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @return the oldRuleDTO
	 */
	public RuleDTO getOldRuleDTO() {
		return oldRuleDTO;
	}

}
