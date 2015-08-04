/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.Set;

import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Condition module is used into "IF" section of the {@link Rule} definition.
 * The "IF" section defines conditions which must be satisfied to continue {@link Rule} execution. Building elements of
 * condition ( {@link ConfigDescriptionParameter}s and {@link Input}s are defined by {@link ConditionType} Conditions
 * don't have {@link Output} elements.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface Condition extends Module {

    /**
     * This method is used to get input connections of the Condition. The
     * connections are links between {@link Input}s of the current {@link Module} and {@link Output}s of other
     * {@link Module}s.
     *
     * @return a {@link Set} of input {@link Connection}s.
     */
    public Set<Connection> getConnections();

    /**
     * This method is used to connect {@link Input}s of the Condition to {@link Output}s of other {@link Module}s.
     *
     * @param connections a {@link Set} of input {@link Connection}s.
     */
    public void setConnections(Set<Connection> connections);

}
