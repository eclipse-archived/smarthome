/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.defaultscope.internal;

import java.io.File;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.module.script.ScriptExtensionProvider;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a default scope provider for stuff that is of general interest in an ESH-based solution.
 * Nonetheless, solutions are free to remove it and have more specific scope providers for their own purposes.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Simon Merschjohann - refactored to be an ScriptExtensionProvider
 *
 */
public class DefaultScriptScopeProvider implements ScriptExtensionProvider {

    private final Logger logger = LoggerFactory.getLogger(DefaultScriptScopeProvider.class);

    private Map<String, Object> elements;

    private ItemRegistry itemRegistry;

    private ThingRegistry thingRegistry;

    private EventPublisher eventPublisher;

    private ScriptBusEvent busEvent;

    private RuleRegistry ruleRegistry;

    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void activate() {
        busEvent = new ScriptBusEvent(itemRegistry, eventPublisher);

        elements = new HashMap<>();
        elements.put("State", State.class);
        elements.put("Command", Command.class);
        try {
            elements.put("DateTime", DateTime.class);
            elements.put("LocalTime", LocalTime.class);
        } catch (NoClassDefFoundError e) {
            logger.debug("Jodatime not present, therefore no support for Date/Time in scripts");
        }
        elements.put("StringUtils", StringUtils.class);
        elements.put("URLEncoder", URLEncoder.class);
        elements.put("FileUtils", FileUtils.class);
        elements.put("FilenameUtils", FilenameUtils.class);
        elements.put("File", File.class);

        // ESH types
        elements.put("IncreaseDecreaseType", IncreaseDecreaseType.class);
        elements.put("DECREASE", IncreaseDecreaseType.DECREASE);
        elements.put("INCREASE", IncreaseDecreaseType.INCREASE);

        elements.put("OnOffType", OnOffType.class);
        elements.put("ON", OnOffType.ON);
        elements.put("OFF", OnOffType.OFF);

        elements.put("OpenClosedType", OpenClosedType.class);
        elements.put("CLOSED", OpenClosedType.CLOSED);
        elements.put("OPEN", OpenClosedType.OPEN);

        elements.put("StopMoveType", StopMoveType.class);
        elements.put("MOVE", StopMoveType.MOVE);
        elements.put("STOP", StopMoveType.STOP);

        elements.put("UpDownType", UpDownType.class);
        elements.put("DOWN", UpDownType.DOWN);
        elements.put("UP", UpDownType.UP);

        elements.put("DateTimeType", DateTimeType.class);
        elements.put("DecimalType", DecimalType.class);
        elements.put("HSBType", HSBType.class);
        elements.put("PercentType", PercentType.class);
        elements.put("PointType", PointType.class);
        elements.put("StringType", StringType.class);

        // services
        elements.put("items", new ItemRegistryDelegate(itemRegistry));
        elements.put("ir", itemRegistry);
        elements.put("itemRegistry", itemRegistry);
        elements.put("things", thingRegistry);
        elements.put("events", busEvent);
        elements.put("rules", ruleRegistry);
    }

    protected void deactivate() {
        busEvent.dispose();
        busEvent = null;
        elements = null;
    }

    @Override
    public Collection<String> getDefaultPresets() {
        return Collections.singleton("default");
    }

    @Override
    public Collection<String> getPresets() {
        return Collections.singleton("default");
    }

    @Override
    public Collection<String> getTypes() {
        return elements.keySet();
    }

    @Override
    public Object get(String scriptIdentifier, String type) {
        return elements.get(type);
    }

    @Override
    public Map<String, Object> importPreset(String scriptIdentifier, String preset) {
        if (preset.equals("default")) {
            return elements;
        }

        return null;
    }

    @Override
    public void unload(String scriptIdentifier) {
        // nothing todo
    }

}
