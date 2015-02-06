/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.upnp;

import java.net.URL;
import java.util.Map;

/**
 * The {@link UpnpIOService} is an interface that described the
 * UPNP IO Service.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - added descriptor url retrieval
 */
public interface UpnpIOService {

    /**
     * Invoke an UPNP Action
     * 
     * @param participant - the participant to invoke the action for
     * @param serivceID - the UPNP service to invoke the action upon
     * @param actionID - the Action to invoke
     * @param inputs - a map of {variable,values} to paramterise the Action that will be invoked
     */
    public Map<String, String> invokeAction(UpnpIOParticipant participant, String serviceID, String actionID,
            Map<String, String> inputs);

    /**
     * Subscribe to a GENA subscription
     * 
     * @param participant - the participant to the subscription is for
     * @param serviceID - the UPNP service we want to subscribe to
     * @param duration - the duration of the subscription
     */
    public void addSubscription(UpnpIOParticipant participant, String serviceID, int duration);

    /**
     * Verify if the a participant is registered
     * 
     * @param participant - the participant whom's participation we want to verify
     * 
     * @return true of the participant is registered with the UpnpIOService
     */
    public boolean isRegistered(UpnpIOParticipant participant);

    /**
     * Retrieves the descriptor url for the participant
     * 
     * @param participant - the participant whom's descriptor url is requested
     * 
     * @return the url of the descriptor as provided by the upnp device
     */
    public URL getDescriptorURL(UpnpIOParticipant participant);

}
