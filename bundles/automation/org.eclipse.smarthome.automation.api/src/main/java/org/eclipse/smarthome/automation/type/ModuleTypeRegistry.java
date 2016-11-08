/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * This interface provides functionality to get available {@link ModuleType}s.
 * The module types can be returned localized depending on locale parameter.
 * When it is not specified or there is no such localization resources the
 * returned module type is localized with default locale.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface ModuleTypeRegistry extends Registry<ModuleType, String> {

    /**
     * This method is used to get localized ModuleType by specified UID and locale.
     *
     * @param moduleTypeUID the an unique id in scope of registered ModuleTypes
     * @param locale used for localization of the ModuleType
     * @return ModuleType instance or null.
     */
    public <T extends ModuleType> T get(String moduleTypeUID, Locale locale);

    /**
     * This method is used for getting the ModuleTypes filtered by tag.
     *
     * @param moduleTypeTag specifies the filter for getting the ModuleTypes, if
     *            it is <code>null</code> then returns all ModuleTypes.
     * @return the ModuleTypes, which correspond to the specified filter.
     */
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag);

    /**
     * This method is used for getting the ModuleTypes filtered by tag.
     *
     * @param moduleTypeTag specifies the filter for getting the ModuleTypes, if
     *            it is <code>null</code> then returns all ModuleTypes.
     * @param locale used for localization of the ModuleType
     * @return the ModuleTypes, which correspond to the specified filter.
     */
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale);

    /**
     * This method is used for getting the ModuleTypes filtered by tags.
     *
     * @param tags specifies the filter for getting the ModuleTypes, if
     *            it is <code>null</code> then returns all ModuleTypes.
     * @return the ModuleTypes, which correspond to the filter.
     */
    public <T extends ModuleType> Collection<T> getByTags(String... tags);

    /**
     * This method is used for getting the ModuleTypes filtered by tags.
     *
     * @param locale used for localization of the ModuleType
     * @param moduleTypeTag specifies the filter for getting the ModuleTypes, if
     *            it is <code>null</code> then returns all ModuleTypes.
     * @return the ModuleTypes, which correspond to the filter.
     */
    public <T extends ModuleType> Collection<T> getByTags(Locale locale, String... tags);

    /**
     * This method is used for getting the {@link TriggerType}s. The returned {@link TriggerType}s are
     * localized by default locale.
     *
     * @return collection of all available {@link TriggerType}s, localized by default locale.
     */
    public Collection<TriggerType> getTriggers();

    /**
     * This method is used for getting the {@link TriggerType}s, localized depending on passed locale parameter.
     * When the locale parameter is not specified or such localization resources are not available the
     * returned {@link TriggerType}s are localized by default locale
     *
     * @param locale defines the localization of returned {@link TriggerType}s.
     * @return a collection of all available {@link TriggerType}s, localized by default locale or the passed locale
     *         parameter.
     */
    public Collection<TriggerType> getTriggers(Locale locale);

    /**
     * This method is used for getting the {@link ConditionType}s. The returned {@link ConditionType}s are
     * localized by default locale.
     *
     * @return collection of all available {@link ConditionType}s, localized by default locale.
     */
    public Collection<ConditionType> getConditions();

    /**
     * This method is used for getting the {@link ConditionType}s, localized depending on passed locale parameter.
     * When the locale parameter is not specified or such localization resources are not available the
     * returned {@link ConditionType}s are localized by default locale
     *
     * @param locale defines the localization of returned {@link ConditionType}s.
     * @return a collection of all available {@link ConditionType}s, localized by default locale or the passed locale
     *         parameter.
     */
    public Collection<ConditionType> getConditions(Locale locale);

    /**
     * This method is used for getting the {@link ActionType}s. The returned {@link ActionType}s are
     * localized by default locale.
     *
     * @return collection of all available {@link ActionType}s, localized by default locale.
     */
    public Collection<ActionType> getActions();

    /**
     * This method is used for getting the {@link ActionType}s, localized depending on passed locale parameter.
     * When the locale parameter is not specified or such localization resources are not available the
     * returned {@link ActionType}s are localized by default locale
     *
     * @param locale defines the localization of returned {@link ActionType}s.
     * @return a collection of all available {@link ActionType}s, localized by default locale or the passed locale
     *         parameter.
     */
    public Collection<ActionType> getActions(Locale locale);

}
