/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.Thing;


/**
 * This class defines the action configuration interface of Eclipse SmartHome
 *
 * @author Chris Jackson - Initial contribution and API
 */
public interface ThingConfigAction {

    /**
     * Notifies the handler when there is a {@link ConfigurationAction} to be processed.
     *
     * @param thing
     *            thing of the config description
     * @param action
     *            the name of the action
     * @param value
     *            the value to be configured
     */
    void handleConfigurationAction(Thing thing, String action, String value);

}
