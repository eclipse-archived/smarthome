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

import java.util.Map;

import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Modules are building components of the {@link Rule}s. Each Module is
 * identified by id, which is unique in scope of the {@link Rule}. It also has a {@link ModuleType} which provides meta
 * data of the module. The meta data
 * defines {@link Input}s, {@link Output}s and {@link ConfigDescriptionParameter}s parameters of the {@link Module}. <br>
 * Setters of the module don't have immediate effect on the Rule. To apply the
 * changes, they should be set on the {@link Rule} and the Rule has to be
 * updated by {@link RuleRegistry}
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public interface Module {

    /**
     * This method is used for getting the id of the {@link Module}. It is unique
     * in scope of the {@link Rule}.
     *
     * @return module id
     */
    public String getId();

    /**
     * This method is used for getting the reference to {@link ModuleType} of this
     * module. The {@link ModuleType} contains description, tags and meta info for
     * this module.
     *
     * @return unique id of the {@link ModuleType} of this {@link Module}.
     */
    public String getTypeUID();

    /**
     * This method is used for getting the label of the Module. The label is a
     * short, user friendly name of the Module.
     *
     * @return the label of the module.
     */
    public String getLabel();

    /**
     * This method is used for setting the label of the Module.
     *
     * @param label of the module.
     */
    public void setLabel(String label);

    /**
     * This method is used for getting the description of the Module. The
     * description is a long, user friendly description of the Module.
     *
     * @return the description of the module.
     */
    public String getDescription();

    /**
     * This method is used for setting the description of the Module.
     *
     * @param description of the module.
     */
    public void setDescription(String description);

    /**
     * This method is used for getting configuration values of the {@link Module}.
     * The key is id of the {@link ConfigDescriptionParameter} and the value is
     * the configuration value.
     *
     * @return current configuration values
     */
    public Map<String, Object> getConfiguration();

    /**
     * This method is used for setting the Map with configuration values of the {@link Module}. Key - id of the
     * {@link ConfigDescriptionParameter} Value -
     * the value of the corresponding property
     *
     * @param configurations new configuration values.
     */
    public void setConfiguration(Map<String, ?> configurations);

}
