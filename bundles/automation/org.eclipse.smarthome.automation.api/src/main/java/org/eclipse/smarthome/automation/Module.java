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
package org.eclipse.smarthome.automation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Modules are the super class of Trigger, Actions and Conditions. The all have an id, a type, a label, a description
 * and a configuration.
 *
 * @author Kai Kreuzer - Initial Contribution
 */
@NonNullByDefault
public interface Module {

    /**
     * This method is used for getting the id of the {@link Module}. It is unique
     * in scope of the {@link Rule}.
     *
     * @return module id
     */
    String getId();

    /**
     * This method is used for getting the reference to {@link ModuleType} of this
     * module. The {@link ModuleType} contains description, tags and meta info for
     * this module.
     *
     * @return unique id of the {@link ModuleType} of this {@link Module}.
     */
    String getTypeUID();

    /**
     * This method is used for getting the label of the Module. The label is a
     * short, user friendly name of the Module.
     *
     * @return the label of the module or null.
     */
    @Nullable
    String getLabel();

    /**
     * This method is used for getting the description of the Module. The
     * description is a long, user friendly description of the Module.
     *
     * @return the description of the module or null.
     */
    @Nullable
    String getDescription();

    /**
     * This method is used for getting configuration values of the {@link Module}.
     *
     * @return current configuration values.
     */
    Configuration getConfiguration();

}