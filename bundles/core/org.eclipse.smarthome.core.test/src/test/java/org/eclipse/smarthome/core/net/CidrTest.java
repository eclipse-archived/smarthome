/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.net;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for NetUtil class
 *
 * @author Stefan Triller - initial contribution
 * @author David Graeff - Port to CidrAddress
 */
public class CidrTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidPrefix() {
        new CidrAddress("192.168.5.8/-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfBoundsPrefix() {
        new CidrAddress("192.168.5.8/33");
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfBoundsIPv6Prefix() {
        new CidrAddress("2001:db8:0:0:8:800:200c:417a/129");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notANumberForPrefix() {
        new CidrAddress("192.168.5.8/TEST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noValidIP() {
        new CidrAddress("TEST");
    }

    @Test
    public void isIP() {
        assertTrue(CidrAddress.isIpAddress("192.168.0.1"));
        assertTrue(CidrAddress.isIpAddress("2001:db8:0:0:8:800:200c:417a"));
        assertTrue(CidrAddress.isIpAddress("2001:DB8:0:0:8:800:200C:417A%eth0"));
    }

    @Test
    public void isInRange() {
        new CidrAddress("192.168.5.8/32");// Test full mask
        assertTrue(
                new CidrAddress("2001:db8:0:0:8:800:200c:417a/33").isInRange(new CidrAddress("2001:db8:0:0:0:0:0:0")));
        assertTrue(new CidrAddress("192.168.0.1/24").isInRange(new CidrAddress("192.168.0.0")));
        assertTrue(new CidrAddress("192.168.23.5/24").isInRange(new CidrAddress("192.168.23.0")));
        assertTrue(new CidrAddress("172.16.42.23/16").isInRange(new CidrAddress("172.16.0.0")));
        assertTrue(new CidrAddress("10.8.13.5/8").isInRange(new CidrAddress("10.0.0.0")));
    }
}
