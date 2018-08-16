/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SimplePortPool}.
 * 
 * @author Florian Stolte - Initial Contribution
 *
 */
public class SimplePortPoolTest {

    private SimplePortPool simplePortPool;

    @Before
    public void setup() {
        this.simplePortPool = new SimplePortPool();
    }

    @Test
    public void testPoolOnlyGivesOutPortsNotInUse() {
        final List<Integer> ports = acquireSomePorts();

        final int newPort = simplePortPool.getNextPort();

        for (int port : ports) {
            assertThat(port, not(is(newPort)));
        }
    }

    @Test
    public void testPoolReusesReleasedPort() {
        final List<Integer> ports = acquireSomePorts();
        final int firstPort = ports.get(0);

        simplePortPool.release(firstPort);
        int newPortAfterRelease = simplePortPool.getNextPort();

        assertThat(newPortAfterRelease, is(firstPort));
    }

    @Test
    public void testPoolSkipsPortsThatAreSetInUse() {
        final int firstPort = simplePortPool.getNextPort();

        simplePortPool.setInUse(firstPort + 1);
        int newPortAfterSetInUse = simplePortPool.getNextPort();

        assertThat(newPortAfterSetInUse, is(firstPort + 2));
    }

    private List<Integer> acquireSomePorts() {
        final int numberOfPorts = 5;
        final List<Integer> ports = new ArrayList<>(numberOfPorts);

        for (int i = 0; i < numberOfPorts; i++) {
            ports.add(simplePortPool.getNextPort());
        }

        return ports;
    }
}
