/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Connection;

/**
 * This internal interface is implemented by modules implementations which have inputs.
 * It is used for unified access to modules with inputs.
 * ConditionImpl and ActionImpl implements this interface.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface ConnectedModule {

    /**
     * Gets unique module type id.
     *
     * @return uid of module type.
     */
    String getTypeUID();

    /**
     * Gets set of connections for this module.
     *
     * @return connection objects of module.
     */
    Set<Connection> getConnections();

    /**
     * Gets map of {@link OutputRef}s connected to module's inputs.
     *
     * @return map inputs and corresponding ouput values.
     */
    Map<String, OutputRef> getConnectedOutputs();

    /**
     * Sets connections between inputs and outputs of other modules. These connections are set by the rule engine when
     * the module is evaluated for the first time.
     *
     * @param connectedOutputs {@link Map} of input and connected {@link OutputRef}s
     */
    void setConnectedOutputs(Map<String, OutputRef> connectedOutputs);

}
