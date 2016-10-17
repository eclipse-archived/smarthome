/**
 * Copyright (c) 1997, 2016 by ProSyst Software GmbH and others.
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
 * This class is a handler for RuleEnablementAction module type.
 * It enables or disables the rules which's UIDs are passed by the 'ruleUIDs' property.
 * !!! If a rule's status is NOT_INITIALIZED that rule can't be enabled. !!!
 *
 * <pre>
 *Example:
 *
 *"id": "RuleAction",
 *"type": "RuleEnablementAction",
 *"configuration": {
 *     "enable": true,
 *     "ruleUIDs": ["UID1", "UID2", "UID3"]
 * }
 * </pre>
 *
 * @author Plamen Peev
 *
 */
public class RuleEnableHandler extends BaseModuleHandler<Action> implements ActionHandler {

    /**
     * This filed contains the type of this handler so it can be recognized from the factory.
     */
    public final static String UID = "RuleEnablementAction";

    /**
     * This field is a key to the 'enable' property of the {@link Action}.
     */
    private final static String ENABLE_KEY = "enable";

    /**
     * This field is a key to the 'rulesUIDs' property of the {@link Action}.
     */
    private final static String RULE_UIDS_KEY = "ruleUIDs";

    /**
     * This logger is used to log warning message if at some point {@link RuleRegistry} service becomes unavailable.
     */
    private final static Logger logger;

    /**
     * This field stores the UIDs of the rules to which the action will be applied.
     */
    private final List<String> UIDs;

    /**
     * This field stores the value for the setEnabled() method of {@link RuleRegistry}.
     */
    private final boolean enable;

    /**
     * Reference to {@link RuleRegistry} service that will be used to enable and disable rules.
     */
    private RuleRegistry ruleRegistry;

    static {
        logger = LoggerFactory.getLogger(RuleEnableHandler.class);
    }

    @SuppressWarnings("unchecked")
    public RuleEnableHandler(final Action module, final RuleRegistry ruleRegistry) {
        super(module);
        final Configuration config = module.getConfiguration();
        if (config == null) {
            throw new IllegalArgumentException("'Configuration' can not be null.");
        }

        final Boolean enable = (Boolean) config.get(ENABLE_KEY);
        if (enable == null) {
            throw new IllegalArgumentException("'enable' property can not be null.");
        }
        this.enable = enable.booleanValue();

        UIDs = (List<String>) config.get(RULE_UIDS_KEY);
        if (UIDs == null) {
            throw new IllegalArgumentException("'ruleUIDs' property can not be null.");
        }

        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> context) {
        for (String uid : UIDs) {
            if (ruleRegistry != null) {
                ruleRegistry.setEnabled(uid, enable);
            } else {
                logger.warn("Action is not applyed to {} because RuleRegistry is not available.", uid);
            }
        }
        return null;
    }
}
