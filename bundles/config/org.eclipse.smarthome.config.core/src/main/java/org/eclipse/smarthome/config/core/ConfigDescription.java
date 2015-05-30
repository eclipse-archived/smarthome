/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link ConfigDescription} class contains a description for a concrete
 * configuration of e.g. a {@code Thing}, a {@code Bridge} or other specific
 * configurable services. This class <i>does not</i> contain the configuration
 * data itself and is usually used for data validation of the concrete
 * configuration or for supporting user interfaces.
 * <p>
 * The {@link ConfigDescriptionParameterGroup} provides a method to group parameters to allow the UI to better display
 * the parameter information. This can be left blank for small devices where there are only a few parameters, however
 * devices with larger numbers of parameters can set the group member in the {@link ConfigDescriptionParameter} and then
 * provide group information as part of the {@link ConfigDescription} class.
 * <p>
 * The {@link ConfigDescriptionAction} provides a method to define configuration actions. These are sent as immediate
 * actions to the thing handler rather than being handled and persisted as parameters. This allows the ThingHandler to
 * support immediate user interaction for configuration purposes such as resetting a device.
 * <p>
 * The description is stored within the {@link ConfigDescriptionRegistry} under the given URI. The URI has to follow the
 * syntax {@code '<scheme>:<token>[:<token>]'} (e.g. {@code "binding:hue:bridge"}).
 * <p>
 * <b>Hint:</b> This class is immutable.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Initial Contribution
 * @author Chris Jackson - Added parameter groups and actions
 */
public class ConfigDescription {

    private URI uri;
    private List<ConfigDescriptionAction> actions;
    private List<ConfigDescriptionParameter> parameters;
    private List<ConfigDescriptionParameterGroup> parameterGroups;

    /**
     * Creates a new instance of this class with the specified parameter.
     *
     * @param uri the URI of this description within the {@link ConfigDescriptionRegistry}
     * @throws IllegalArgumentException if the URI is null or invalid
     */
    public ConfigDescription(URI uri) throws IllegalArgumentException {
        this(uri, null, null, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uri the URI of this description within the {@link ConfigDescriptionRegistry} (must neither be null nor
     *            empty)
     *
     * @param parameters the description of a concrete configuration parameter
     *            (could be null or empty)
     *
     * @throws IllegalArgumentException if the URI is null or invalid
     */
    public ConfigDescription(URI uri, List<ConfigDescriptionParameter> parameters) {
        this(uri, parameters, null, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uri the URI of this description within the {@link ConfigDescriptionRegistry} (must neither be null nor
     *            empty)
     *
     * @param parameters the description of a concrete configuration parameter
     *            (could be null or empty)
     *
     * @param groups the list of groups associated with the parameters
     *
     * @throws IllegalArgumentException if the URI is null or invalid
     */
    public ConfigDescription(URI uri, List<ConfigDescriptionParameter> parameters,
            List<ConfigDescriptionParameterGroup> groups) {
        this(uri, parameters, groups, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uri the URI of this description within the {@link ConfigDescriptionRegistry} (must neither be null nor
     *            empty)
     *
     * @param parameters the description of a concrete configuration parameter
     *            (could be null or empty)
     * 
     * @param groups the list of groups associated with the parameters
     *
     * @throws IllegalArgumentException if the URI is null or invalid
     */
    public ConfigDescription(URI uri, List<ConfigDescriptionParameter> parameters,
            List<ConfigDescriptionParameterGroup> groups, List<ConfigDescriptionAction> actions) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null!");
        }
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("The scheme is missing!");
        }
        if (!uri.isOpaque()) {
            throw new IllegalArgumentException("The scheme specific part (token) must not start with a slash ('/')!");
        }

        this.uri = uri;

        if (parameters != null) {
            this.parameters = Collections.unmodifiableList(parameters);
        } else {
            this.parameters = Collections.unmodifiableList(new ArrayList<ConfigDescriptionParameter>(0));
        }

        if (groups != null) {
            this.parameterGroups = Collections.unmodifiableList(groups);
        } else {
            this.parameterGroups = Collections.unmodifiableList(new ArrayList<ConfigDescriptionParameterGroup>(0));
        }

        if (actions != null) {
            this.actions = Collections.unmodifiableList(actions);
        } else {
            this.actions = Collections.unmodifiableList(new ArrayList<ConfigDescriptionAction>(0));
        }
    }

    /**
     * Returns the URI of this description within the {@link ConfigDescriptionRegistry}.
     * The URI follows the syntax {@code '<scheme>:<token>[:<token>]'} (e.g. {@code "binding:hue:bridge"}).
     * 
     * @return the URI of this description (not null)
     */
    public URI getURI() {
        return this.uri;
    }

    /**
     * Returns the description of a concrete configuration parameter.
     * <p>
     * The returned list is immutable.
     *
     * @return the description of a concrete configuration parameter (not null, could be empty)
     */
    public List<ConfigDescriptionParameter> getParameters() {
        return this.parameters;
    }

    /**
     * Returns the list of configuration parameter groups associated with the parameters.
     * <p>
     * The returned list is immutable.
     *
     * @return the list of parameter groups parameter (not null, could be empty)
     */
    public List<ConfigDescriptionParameterGroup> getParameterGroups() {
        return this.parameterGroups;
    }

    /**
     * Returns the description of a concrete configuration action.
     * <p>
     * The returned list is immutable.
     *
     * @return the description of a concrete configuration action (not null, could be empty)
     */
    public List<ConfigDescriptionAction> getActions() {
        return this.actions;
    }

    @Override
    public String toString() {
        return "ConfigDescription [uri=" + uri + ", parameters=" + parameters + ", groups=" + parameterGroups
                + ", actions=" + actions + "]";
    }

}
