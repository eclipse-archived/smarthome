/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class defines common implementation of all modules (triggers, conditions and actions).
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public abstract class ModuleImpl<T extends ModuleHandler> implements Module {

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
    protected Map<String, Object> configuration;

    /**
     * The handler of this module.
     */
    private T moduleHandler;

    /**
     * Unique type id of this module.
     */
    private String typeUID;

    /**
     * Constructor of the module.
     *
     * @param id the module id.
     * @param typeUID unique id of the module type.
     * @param configuration configuration values of the module.
     */
    public ModuleImpl(String id, String typeUID, Map<String, ?> configuration) {
        this.id = id;
        this.typeUID = typeUID;
        setConfiguration(configuration);
    }

    /**
     * Utility constructor for module cloning.
     *
     * @param module a module which has to be cloned.
     */
    protected ModuleImpl(ModuleImpl<T> module) {
        this(module.getId(), module.getTypeUID(), new HashMap<String, Object>(module.getConfiguration()));
        setLabel(module.getLabel());
        setDescription(module.getDescription());
    }

    /**
     * This method is used for getting the {@link #id} of the {@link Module}.
     *
     * @return module id
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTypeUID() {
        return typeUID;
    }

    /**
     * This method is used for getting the label of the {@link Module}. The label
     * is a short, user friendly name of the {@link Module} defined by this
     * descriptor.
     *
     * @return the label of the module.
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for setting the label of the {@link Module}. The label
     * is a short, user friendly name of the {@link Module} defined by this
     * descriptor.
     *
     * @param label of the module.
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method is used for getting the description of the {@link Module}. The
     * description is a long, user friendly description of the {@link Module} defined by this descriptor.
     *
     * @return the description of the module.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the {@link Module}. The
     * description is a long, user friendly description of the {@link Module} defined by this descriptor.
     *
     * @param description of the module.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration != null ? new HashMap<String, Object>(configuration) : null;
    }

    /**
     * Utility method creating deep copy of passed connection set.
     *
     * @param connections connections used by this module.
     * @return copy of passed connections.
     */
    protected Set<Connection> copyConnections(Set<Connection> connections) {
        if (connections == null) {
            return null;
        }
        Set<Connection> result = new HashSet<Connection>(connections.size());
        for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
            Connection c = it.next();
            result.add(new Connection(c.getInputName(), c.getOuputModuleId(), c.getOutputName()));
        }
        return result;
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     * 
     * @return handler of the module or null.
     */
    protected T getModuleHandler() {
        return moduleHandler;
    }

    /**
     * This method sets handler of the module.
     * 
     * @param moduleHandler
     */
    protected void setModuleHandler(T moduleHandler) {
        this.moduleHandler = moduleHandler;
    }

}
