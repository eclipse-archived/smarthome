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

import java.util.Set;

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Actions are the part of "THEN" section of the {@link Rule} definition.
 * Elements of this section are expected result of {@link Rule} execution. The
 * Action can have {@link Output} elements. These actions are used to process
 * input data as source data of other Actions. Building elements of actions ( {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s) are
 * defined by {@link ActionType}
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface Action extends Module {

    /**
     * This method is used to get input connections of the Action. The connections
     * are links between {@link Input}s of the {@link Module} and {@link Output}s
     * of other {@link Module}s.
     *
     * @return a {@link Set} of input {@link Connection}s.
     */
    public Set<Connection> getConnections();

    /**
     * This method is used to connect {@link Input}s of the action to {@link Output}s of other {@link Module}s.
     *
     * @param connections a {@link Set} of input {@link Connection}s.
     */
    public void setConnections(Set<Connection> connections);

}
