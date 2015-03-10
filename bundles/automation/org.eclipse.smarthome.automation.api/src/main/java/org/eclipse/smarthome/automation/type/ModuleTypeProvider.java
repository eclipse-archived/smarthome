/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation.type;

import java.util.Collection;
import java.util.Locale;

/**
 * This interface has to be implemented by all providers of {@link ModuleType}s.
 * The {@link ModuleTypeRegistry} uses it to get access to available {@link ModuleType}s.
 *
 * @author Yordan Mihaylov
 */
public interface ModuleTypeProvider {

    /**
     * This method is used to get localized ModuleType. When the localization is
     * not specified or it is not supported a ModuleType with default locale is
     * returned.
     *
     * @param UID unique id of module type.
     * @param locale defines localization of label and description of the {@link ModuleType} or null.
     * @return localized module type.
     */
    ModuleType getModuleType(String UID, Locale locale);

    /**
     * This method is used to get localized ModuleTypes defined by this provider.
     * When localization is not specified or it is not supported a ModuleTypes
     * with default localization is returned.
     *
     * @param locale defines localization of label and description of the {@link ModuleType}s or null.
     * @return collection of localized {@link ModuleType} provided by this
     *         provider
     */
    Collection<ModuleType> getModuleTypes(Locale locale);

}
