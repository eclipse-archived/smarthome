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

package org.eclipse.smarthome.automation.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.type.Input;

/**
 * This interface is implemented by modules which have inputs.
 * ConditionImpl and ActionImpl implements this interface.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface ConnectedModule {

    String getTypeUID();

    Set<Connection> getConnections();

    Map<String, OutputValue> getConnectedObjects();

    void setConnectedObjects(Map<String, OutputValue> connectedObjects);

    Map<Input, List<Input>> getInputMap();

    void setInputMap(Map<Input, List<Input>> map);
}
