/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.type;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * This interface has to be implemented by all providers of {@link ModuleType}s.
 * The {@link ModuleTypeRegistry} uses it to get access to available {@link ModuleType}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Ana Dimova - add registration property - module.types
 */
public interface ModuleTypeProvider extends Provider<ModuleType> {

    /**
     * This method is used to get localized ModuleType. When the localization is
     * not specified or it is not supported a ModuleType with default locale is
     * returned.
     *
     * @param UID    unique id of module type.
     * @param locale defines localization of label and description of the {@link ModuleType} or null.
     * @param        <T> the type of the required object.
     * @return localized module type.
     */
    <T extends ModuleType> T getModuleType(String UID, Locale locale);

    /**
     * This method is used to get localized ModuleTypes defined by this provider.
     * When localization is not specified or it is not supported a ModuleTypes
     * with default localization is returned.
     *
     * @param locale defines localization of label and description of the {@link ModuleType}s or null.
     * @param        <T> the type of the required object.
     * @return collection of localized {@link ModuleType} provided by this provider.
     */
    <T extends ModuleType> Collection<T> getModuleTypes(Locale locale);

}
