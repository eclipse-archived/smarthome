/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation;

import java.util.Collection;

/**
 * This class defines event topics and event properties which are used by the
 * automation events.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface RuleEventConstants {

    /**
     * Event property which contains uid of the Rule
     */
    public static final String RULE_UID = "ruleUID"; //$NON-NLS-1$

    /**
     * Event property which contains NAME of the Rule
     */
    public static final String RULE_NAME = "ruleName"; //$NON-NLS-1$

    /**
     * Event property which contains tags of the Rule. Property value is {@link Collection} of tags
     */
    public static final String RULE_TAGS = "ruleTags"; //$NON-NLS-1$

    /**
     * Event property which shows if the Rule is enabled or not.
     */
    public static final String RULE_ENABLED = "isEnabled"; //$NON-NLS-1$

    /**
     * Event property which shows if the Rule is running or not.
     */
    public static final String RULE_RUNNING = "isRunning"; //$NON-NLS-1$

    /**
     * Event property which contains type of the Module. The type can be template
     * type, when the module is created by template or manual defined by user.
     */
    public static final String MODULE_TYPE = "moduleType"; //$NON-NLS-1$

    /**
     * Event property which contains uid of the Module.
     */
    public static final String MODULE_UID = "moduleUID"; //$NON-NLS-1$

    /**
     * Event property which shows if the Condition is running or not.
     */
    public static final String CONDITION_SATISFIED = "isSatisfied"; //$NON-NLS-1$

    /**
     * Event property which shows the result of Command execution.
     */
    public static final String COMMAND_EXECUTION_STATUS = "executionStatus"; //$NON-NLS-1$

    /**
     * Event property which contains output values of the Module. The value of
     * this property is Map where the keys are equal to output uid, and values are
     * current values set to the output.
     */
    public static final String MODULE_OUTPUT_VALUES = "outputs"; //$NON-NLS-1$

    /**
     * Event property which contains input values of the Module. The value of this
     * property is Map where the keys are equal to input uid, and values are
     * current values set to the input.
     */
    public static final String MODULE_INPUT_VALUES = "inputs"; //$NON-NLS-1$

    /**
     * Topic of rule enabled event. This event is fired when the rule change its
     * enable/disable state. The event contains following properties:
     * <ul>
     * <li>property: {@link #RULE_UID} @link
     * <li>property: {@link #RULE_NAME}
     * <li>property: {@link #RULE_TAGS}
     * <li>property: {@link #RULE_ENABLED}
     * <li>property: {@link #RULE_RUNNING}
     * </ul>
     */
    public static final String TOPIC_RULE_ENABLED = "org/eclipse/smarthome/automation/rule/enabled"; //$NON-NLS-1$

    /**
     * Topic of event fired by the rule when the rule change its running state.
     * The event contains following properties:
     * <ul>
     * <li>property: {@link #RULE_UID}
     * <li>property: {@link #RULE_NAME}
     * <li>property: {@link #RULE_TAGS}
     * <li>property: {@link #RULE_ENABLED}
     * <li>property: {@link #RULE_RUNNING}
     * </ul>
     */
    public static final String TOPIC_RULE_RUNNING = "org/eclipse/smarthome/automation/rule/running"; //$NON-NLS-1$

    /**
     * Topic of event fired by Trigger module when the trigger receive event and
     * fill its outputs. The event contains following properties:
     * <ul>
     * <li>property: {@link #RULE_UID}
     * <li>property: {@link #RULE_NAME}
     * <li>property: {@link #RULE_TAGS}
     * <li>property: {@link #MODULE_TYPE}
     * <li>property: {@link #MODULE_UID}
     * <li>property: {@link #MODULE_OUTPUT_VALUES}
     * </ul>
     */
    public static final String TOPIC_TRIGGER_TRIGGERED = "org/eclipse/smarthome/automation/rule/trigger/triggered"; //$NON-NLS-1$

    /**
     * Topic of event fired by Condition modules when the Condition changes its
     * satisfied state. The event contains following properties:
     * <ul>
     * <li>property: {@link #RULE_UID}
     * <li>property: {@link #RULE_NAME}
     * <li>property: {@link #RULE_TAGS}
     * <li>property: {@link #MODULE_TYPE}
     * <li>property: {@link #MODULE_UID}
     * <li>property: {@link #MODULE_INPUT_VALUES}
     * <li>property: {@link #CONDITION_SATISFIED}
     * </ul>
     */
    public static final String TOPIC_CONDITION_SATISFIED = "org/eclipse/smarthome/automation/rule/condition/satisfied"; //$NON-NLS-1$

    /**
     * Topic of event fired by {@link Action} modules when the Command finishes
     * its execution. The event contains following properties:
     * <ul>
     * <li>property: {@link #RULE_UID}
     * <li>property: {@link #RULE_NAME}
     * <li>property: {@link #RULE_TAGS}
     * <li>property: {@link #MODULE_TYPE}
     * <li>property: {@link #MODULE_UID}
     * <li>property: {@link #MODULE_INPUT_VALUES}
     * <li>property: {@link #MODULE_OUTPUT_VALUES}
     * <li>property: {@link #COMMAND_EXECUTION_STATUS}
     * </ul>
     */
    public static final String TOPIC_ACTION_EXECUTED = "org/eclipse/smarthome/automation/rule/action/executed"; //$NON-NLS-1$

}
