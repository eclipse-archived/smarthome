/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Modules are building components of the {@link Rule}s. Each Module is
 * identified by id, which is unique in scope of the {@link Rule}. It also has a {@link ModuleType} which provides meta
 * data of the module. The meta data
 * defines {@link Input}s, {@link Output}s and {@link ConfigDescriptionParameter}s parameters of the {@link Module}.
 * <br>
 * Setters of the module don't have immediate effect on the Rule. To apply the
 * changes, they should be set on the {@link Rule} and the Rule has to be
 * updated by RuleManager
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public abstract class Module {

    /**
     * Id of the Module. It is mandatory and unique identifier in scope of the {@link Rule}. The id of the
     * {@link Module} is used to identify the module
     * in the {@link Rule}.
     */
    protected String id;

    /**
     * The label is a short, user friendly name of the {@link Module} defined by
     * this descriptor.
     */
    protected String label;

    /**
     * The description is a long, user friendly description of the {@link Module} defined by this descriptor.
     */
    protected String description;

    /**
     * Configuration values of the Module.
     *
     * @see {@link ConfigDescriptionParameter}.
     */
    protected Configuration configuration;
    /**
     * Unique type id of this module.
     */
    protected String type;

    /**
     * Constructor of the module.
     *
     * @param id the module id.
     * @param typeUID unique id of the module type.
     * @param configuration configuration values of the module.
     */
    public Module(String id, String typeUID, Configuration configuration) {
        this.id = id;
        this.type = typeUID;
        setConfiguration(configuration);
    }

    public Module() {
    }

    /**
     * This method is used for getting the id of the {@link Module}. It is unique
     * in scope of the {@link Rule}.
     *
     * @return module id
     */
    public String getId() {
        return id;
    }

    /**
     * This method is used for getting the reference to {@link ModuleType} of this
     * module. The {@link ModuleType} contains description, tags and meta info for
     * this module.
     *
     * @return unique id of the {@link ModuleType} of this {@link Module}.
     */
    public String getTypeUID() {
        return type;
    }

    /**
     * This method is used for getting the label of the Module. The label is a
     * short, user friendly name of the Module.
     *
     * @return the label of the module or null.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for setting the label of the Module.
     *
     * @param label of the module.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method is used for getting the description of the Module. The
     * description is a long, user friendly description of the Module.
     *
     * @return the description of the module or null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the Module.
     *
     * @param description of the module.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is used for getting configuration values of the {@link Module}.
     *
     * @return current configuration values or null.
     */
    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }

    /**
     * This method is used for setting the configuration of the {@link Module}.
     *
     * @param configuration new configuration values.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;

    }

}
