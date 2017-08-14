/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Tests for NetUtil class
 *
 * @author Stefan Triller - initial contribution
 *
 */
public class NetUtilTest {

    @Test
    public void testNetwork() {
        String network = NetUtil.getIpv4NetAddress("192.168.0.1", (short) 24);
        assertThat(network, is("192.168.0.0"));

        network = NetUtil.getIpv4NetAddress("192.168.23.5", (short) 24);
        assertThat(network, is("192.168.23.0"));

        network = NetUtil.getIpv4NetAddress("172.16.42.23", (short) 16);
        assertThat(network, is("172.16.0.0"));

        network = NetUtil.getIpv4NetAddress("10.8.13.5", (short) 8);
        assertThat(network, is("10.0.0.0"));

        network = NetUtil.getIpv4NetAddress("192.168.5.8", (short) 23);
        assertThat(network, is("192.168.4.0"));

        network = NetUtil.getIpv4NetAddress("192.168.5.8", (short) 27);
        assertThat(network, is("192.168.5.0"));

        network = NetUtil.getIpv4NetAddress("192.168.5.8", (short) 29);
        assertThat(network, is("192.168.5.8"));

        try {
            network = NetUtil.getIpv4NetAddress("192.168.5.8", (short) 32);
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Netmask '32' is out of bounds (1-31)"));
        }
        try {
            network = NetUtil.getIpv4NetAddress("192.168.58", (short) 24);
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("IP '192.168.58' is not a valid IPv4 address"));
        }
        try {
            network = NetUtil.getIpv4NetAddress("SOME_TEXT", (short) 24);
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("IP 'SOME_TEXT' is not a valid IPv4 address"));
        }
        try {
            network = NetUtil.getIpv4NetAddress("SOME_TEXT", (short) 42);
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("IP 'SOME_TEXT' is not a valid IPv4 address"));
        }
    }

}
