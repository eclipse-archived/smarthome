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

package org.eclipse.smarthome.automation.type;

import java.util.Collection;
import java.util.Locale;

/**
 * This interface provides functionality to get available {@link ModuleType}s.
 * The module types can be returned localized depending on locale parameter.
 * When it is not specified or there is no such localization resources the
 * returned module type is localized with default locale.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface ModuleTypeRegistry {

    /**
     * This method is used to get ModuleType of specified by type.
     *
     * @param moduleTypeUID the an unique id in scope of registered ModuleTypes
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
    public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale);

    /**
     * This method is used for getting the ModuleTypes, specified by type module,
     * i.e. {@link ActionModuleType}, {@link ConditionModuleType}, {@link TriggerModuleType} and etc.
     *
     * @param moduleType the class of module which is looking for.
     * @return collection of ModuleTypes, corresponding to specified type
     */
    public <T extends ModuleType> Collection<T> get(Class<T> moduleType, Locale locale);

}
