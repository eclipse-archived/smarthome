/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public interface JSONStructureConstants {

    /**
     * This constant is used as json property that serves to define a set of {@link ActionType}s, when parsing the json
     * files, containing them or to define a set of {@link Action}s when parsing json files, containing
     * {@link CompositeActionType}s, {@link RuleTemplate}s or {@link Rule}s.
     */
    public final String ACTIONS = "actions";

    /**
     * This constant is used as json property that serves to define a set of {@link CompositeActionType}s,
     * {@link CompositeConditionType}s or {@link CompositeTriggerType}s, when parsing the json files, containing them.
     */
    public final String COMPOSITE = "composite";

    /**
     * This constant is used as json property that serves to define a set of {@link ConditionType}s, when parsing the
     * json files, containing them or to define a set of {@link Condition}s when parsing json files, containing
     * {@link CompositeConditionType}s, {@link RuleTemplate}s or {@link Rule}s.
     */
    public final String CONDITIONS = "conditions";

    /**
     * This constant is used as json property that serves to define a set of {@link ConfigDescriptionParameter}s in case
     * of parsing the {@link ModuleType}s. In case of parsing {@link Module}s, it defines a set of key-value pairs,
     * where <b>key</b> is the name of the {@link ConfigDescriptionParameter} and <b>value</b> is its value.
     */
    public final String CONFIG = "config";

    /**
     * This constant is used as json property that serves to define a context for {@link ConfigDescriptionParameter}.
     */
    public final String CONTEXT = "context";

    /**
     * This constant is used as json property that serves to define a filter criteria for
     * {@link ConfigDescriptionParameter}, {@link Input}, {@link Output}.
     */
    public final String DEFAULT_VALUE = "defaultValue";

    /**
     * This constant is used as json property that serves to define a name to all automation objects that have a
     * description.
     */
    public final String DESCRIPTION = "description";

    /**
     * This constant is used as json property that serves to define a filter criteria for
     * {@link ConfigDescriptionParameter}.
     */
    public final String FILTER_CRITERIA = "filterCriteria";

    /**
     * This constant is used as json property that serves to define an ID of the {@link Module}s building the composite
     * {@link ModuleType}s, {@link RuleTemplate}s or {@link Rule}s, when parsing the json files, containing them.
     */
    public final String ID = "id";

    /**
     * This constant is used as json property that serves to define <b>IF</b> section of the {@link Rule}s and
     * {@link RuleTemplate}s.
     */
    public final String IF = "if";

    /**
     * This constant is used as json property that serves to define {@link Input} objects.
     */
    public final String INPUT = "input";

    /**
     * This constant is used as json property that serves to define a name to all automation objects that have a label.
     */
    public final String LABEL = "label";

    /**
     * This constant is used as json property that serves to define a max value for {@link ConfigDescriptionParameter}.
     */
    public final String MAX = "max";

    /**
     * This constant is used as json property that serves to define a min value for {@link ConfigDescriptionParameter}.
     */
    public final String MIN = "min";

    /**
     * This constant is used as json property that serves to define if the {@link ConfigDescriptionParameter} is
     * multiple.
     */
    public final String MULTIPLE = "multiple";

    /**
     * This constant is used as json property that serves to define a name to all automation objects that have a name.
     */
    public final String NAME = "name";

    /**
     * This constant is used as json property that serves to define <b>ON</b> section of the {@link Rule}s and
     * {@link RuleTemplate}s.
     */
    public final String ON = "on";

    /**
     * This constant is used as json property that serves to define options for {@link ConfigDescriptionParameter}.
     */
    public final String OPTIONS = "options";

    /**
     * This constant is used as json property that serves to define {@link Output} objects.
     */
    public final String OUTPUT = "output";

    /**
     * This constant is used as json property that serves to define a pattern for {@link ConfigDescriptionParameter}.
     */
    public final String PATTERN = "pattern";

    /**
     * This constant is used as json property that serves to define if the {@link ConfigDescriptionParameter} is
     * read only.
     */
    public final String READ_ONLY = "readOnly";

    /**
     * This constant is used as json property that serves to define reference in the {@link Input} and {@link Output}
     * objects.
     */
    public final String REFERENCE = "reference";

    /**
     * This constant is used as json property that serves to define if the {@link ConfigDescriptionParameter} is
     * required.
     */
    public final String REQUIRED = "required";

    /**
     * This constant is used as utility string for logging errors in json files provided {@link RuleTemplate}s.
     */
    public final String RULE_TEMPLATES = "ruleTemplates";

    /**
     * This constant is used as json property that serves to define a step for {@link ConfigDescriptionParameter}.
     */
    public final String STEP = "step";

    /**
     * This constant is used as json property that serves to define a set of tags to the {@link ModuleType}s,
     * {@link RuleTemplate}s or {@link Rule}s, when parsing the json files, containing them.
     */
    public final String TAGS = "tags";

    /**
     * This constant is used as json property that serves to define which template tu be used for creation of the
     * {@link Rule}.
     */
    public final String TEMPLATE_UID = "template.uid";

    /**
     * This constant is used as json property that serves to define <b>THEN</b> section of the {@link Rule}s and
     * {@link RuleTemplate}s.
     */
    public final String THEN = "then";

    /**
     * This constant is used as json property that serves to define a set of {@link TriggerType}s, when parsing the
     * json files, containing them or to define a set of {@link Trigger}s when parsing json files, containing
     * {@link CompositeTriggerType}s, {@link RuleTemplate}s or {@link Rule}s.
     */
    public final String TRIGGERS = "triggers";

    /**
     * This constant is used as json property that serves to define a type for {@link ConfigDescriptionParameter},
     * {@link Input}, {@link Output} and {@link Module}s.
     */
    public final String TYPE = "type";

    /**
     * This constant is used as json property that serves to define a UID of the {@link ModuleType}s,
     * {@link RuleTemplate}s or {@link Rule}s, when parsing the json files, containing them.
     */
    public final String UID = "uid";

    /**
     * This constant is used as json property that serves to define a value for {@link ConfigDescriptionParameter}.
     */
    public final String VALUE = "value";

    /**
     * This constant is used as json property that serves to define the visibility of the {@link ModuleType}s,
     * {@link RuleTemplate}s or {@link Rule}s, when parsing the json files, containing them.
     */
    public final String VISIBILITY = "visibility";
}
