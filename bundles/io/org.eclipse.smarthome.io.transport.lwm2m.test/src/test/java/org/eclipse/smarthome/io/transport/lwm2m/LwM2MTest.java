/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.lwm2m;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.naming.ConfigurationException;
import org.eclipse.leshan.server.registration.Registration;
import org.junit.Test;

/**
 * Tests the LwM2MBrokerConnection class
 *
 * @author David Graeff - Initial contribution
 */
public class LwM2MTest {
    @Test
    public void testConstructor() throws ConfigurationException {
        try {
            Link[] objectLinks = { new Link("/3200") };
            return new Registration.Builder("clientIDDemo", "demo", InetAddress.getByName("127.0.0.1"),
                    LwM2m.DEFAULT_COAP_PORT, new InetSocketAddress(12345)).objectLinks(objectLinks).build();
        } catch (UnknownHostException ignored) {
        }

    }

}
