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

/**
 * This interface has to be implemented by all providers of {@link ModuleType}s.
 * The {@link ModuleTypeRegistry} uses it to get access to available {@link ModuleType}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Ana Dimova - add registration property - module.types
 */
public interface ModuleTypeProvider {

    /**
     * This constant is used as registration property for Module Types Provider when the provider expects to modify
     * the provided objects dynamically. Its value is a set of current provided module types UIDs.
     */
    public static final String REG_PROPERTY_MODULE_TYPES = "module.types";

    /**
     * This method is used to get localized ModuleType. When the localization is
     * not specified or it is not supported a ModuleType with default locale is
     * returned.
     *
     * @param UID unique id of module type.
     * @param locale defines localization of label and description of the {@link ModuleType} or null.
     * @return localized module type.
     */
    <T extends ModuleType> T getModuleType(String UID, Locale locale);

    /**
     * This method is used to get localized ModuleTypes defined by this provider.
     * When localization is not specified or it is not supported a ModuleTypes
     * with default localization is returned.
     *
     * @param locale defines localization of label and description of the {@link ModuleType}s or null.
     * @return collection of localized {@link ModuleType} provided by this
     *         provider
     */
    <T extends ModuleType> Collection<T> getModuleTypes(Locale locale);

}
