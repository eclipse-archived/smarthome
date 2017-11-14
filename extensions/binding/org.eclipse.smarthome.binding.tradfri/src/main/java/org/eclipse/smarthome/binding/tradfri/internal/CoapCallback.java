/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

import com.google.gson.JsonElement;

/**
 * The {@link CoapCallback} is receives coap response data asynchronously.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface CoapCallback {

    /**
     * This is being called, if new data is received from a CoAP request.
     *
     * @param data the received json structure
     */
    public void onUpdate(JsonElement data);

    /**
     * Tells the listener to set the Thing status.
     * Should usually be directly passed on to updateStatus() on the ThingHandler.
     *
     * @param status The thing status
     * @param statusDetail the status detail
     */
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail);
}
