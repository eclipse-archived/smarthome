/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.lwm2m.api;

import java.net.InetAddress;
import java.util.Collection;

/**
 *
 * @author David Graeff - Initial contribution
 */
public interface LwM2MClient {
    void registerClientObserver(ClientObserver observer);

    void unregisterClientObserver(ClientObserver observer);

    String getEndpoint();

    InetAddress getAddress();

    ClientStatus getStatus();

    Collection<LwM2MObject> getObjects();

    LwM2MObject getObject(int oID);

    void refresh(OperationCallback callback);
}
