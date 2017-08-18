/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.lwm2m;

import java.util.Collection;
import java.util.Map;

import org.eclipse.smarthome.io.transport.lwm2m.api.ClientRegistry;
import org.eclipse.smarthome.io.transport.lwm2m.api.LwM2MClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LwM2M Service for starting/stopping a LwM2M server instance.
 * TODO:
 * - Notify of new/removed devices
 * - Periodic querying/heart-beat
 * - Configuration via text file: modified/activated with default config
 * - Complete parsing of lwm2m client to ClientDevice/Object/ObjectInstance/Resource tree.
 * Every tree element is observable. The values of the tree element are already in ESH types.
 * - Resource elements have a "setValue(Command)" Method to manipulate the value.
 * - Resource elements have a "getValue():State" Method to return the value.
 * - ObjectInstance elements have a map to resolve a ResID to a Resource object. Multiple
 * ResIDs could point to the same Resource.
 * - A Resource would have a Class<?> or string(classname) field to indicate which ESH type is
 * encoded. A Resource is only an interface for IntegerResource, DoubleResource, StringResource,
 * RollershutterResource extends IntegerResource (UpDownType,StopMoveType,PercentType accepted),
 * DimmerResource extends IntegerResource (OnOffType/IncDecType/PercentType accepted)
 * - A ResourceFactory creates Resources
 *
 * -> ESH wide SSLConfigurationService
 *
 * @author David Graeff - Initial contribution
 */
public class LwM2MServerService implements ClientRegistry {
    private final Logger logger = LoggerFactory.getLogger(LwM2MServerService.class);

    public void activate(Map<String, Object> config) {
        logger.debug("Starting LwM2M Service");
        modified(config);
    }

    public void deactivate() {
        logger.debug("Stopping LwM2M Service");
    }

    public void modified(Map<String, Object> config) {
        logger.debug("Configuration of LwM2M Service changed");
    }

    @Override
    public Collection<LwM2MClient> getClients() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LwM2MClient getClient(String endpoint) {
        // TODO Auto-generated method stub
        return null;
    }

}
