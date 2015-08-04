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
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
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

    private T moduleHandler;

    private String typeUID;

    public ModuleImpl(String id, String typeUID, Map<String, ?> configuration) {
        this.id = id;
        this.typeUID = typeUID;
        setConfiguration(configuration);
    }

    protected ModuleImpl(ModuleImpl m) {
        this(m.getId(), m.getTypeUID(), new HashMap<String, Object>(m.getConfiguration()));
        setLabel(m.getLabel());
        setDescription(m.getDescription());
    }

    /**
     * This method is used for getting the {@link #id} of the {@link Module}.
     *
     * @return module id
     */
    public String getId() {
        return id;
    }

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
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method is used for getting the description of the {@link Module}. The
     * description is a long, user friendly description of the {@link Module} defined by this descriptor.
     *
     * @return the description of the module.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the {@link Module}. The
     * description is a long, user friendly description of the {@link Module} defined by this descriptor.
     *
     * @param description of the module.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration != null ? new HashMap<String, Object>(configuration) : null;
    }

    /**
     * @param connections2
     * @return
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

    protected T getModuleHandler() {
        return moduleHandler;
    }

    protected void setModuleHandler(T moduleHandler) {
        this.moduleHandler = moduleHandler;
    }

}
