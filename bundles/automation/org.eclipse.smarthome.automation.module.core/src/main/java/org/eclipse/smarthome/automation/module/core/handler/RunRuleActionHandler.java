/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a handler for RunRuleAction module type. It runs the rules
 * which's UIDs are passed by the 'ruleUIDs' property. If a rule's status is not
 * IDLE that rule can not run!
 *
 * <pre>
 *Example:
 *
 *"id": "RuleAction",
 *"type": "core.RunRuleAction",
 *"configuration": {
 *     "ruleUIDs": ["UID1", "UID2", "UID3"]
 * }
 * </pre>
 *
 * @author Benedikt Niehues initial contribution
 *
 */
public class RunRuleActionHandler extends BaseModuleHandler<Action> implements ActionHandler {

    /**
     * The UID for this handler for identification in the factory.
     */
    public final static String UID = "core.RunRuleAction";

    /**
     * the key for the 'rulesUIDs' property of the {@link Action}.
     */
    private final static String RULE_UIDS_KEY = "ruleUIDs";
    private final static String CONSIDER_CONDITIONS_KEY = "considerConditions";

    /**
     * The logger
     */
    private final Logger logger = LoggerFactory.getLogger(RunRuleActionHandler.class);

    /**
     * the UIDs of the rules to be executed.
     */
    private final List<String> ruleUIDs;

    /**
     * boolean to express if the conditions should be considered, defaults to
     * true;
     */
    private boolean considerConditions = true;

    /**
     * the {@link RuleRegistry} is used to run rules.
     */
    private RuleRegistry ruleRegistry;

    @SuppressWarnings("unchecked")
    public RunRuleActionHandler(final Action module, final RuleRegistry ruleRegistry) {
        super(module);
        final Configuration config = module.getConfiguration();
        if (config == null) {
            throw new IllegalArgumentException("'Configuration' can not be null.");
        }

        ruleUIDs = (List<String>) config.get(RULE_UIDS_KEY);
        if (ruleUIDs == null) {
            throw new IllegalArgumentException("'ruleUIDs' property must not be null.");
        }
        if (config.get(CONSIDER_CONDITIONS_KEY) != null && config.get(CONSIDER_CONDITIONS_KEY) instanceof Boolean) {
            this.considerConditions = ((Boolean) config.get(CONSIDER_CONDITIONS_KEY)).booleanValue();
        }

        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        // execute each rule after the other; at the moment synchronously
        for (String uid : ruleUIDs) {
            if (ruleRegistry != null) {
                ruleRegistry.runNow(uid, considerConditions, context);
            } else {
                logger.warn("Action is not applied to {} because RuleRegistry is not available.", uid);
            }
        }
        // no outputs from this module
        return null;
    }
}
