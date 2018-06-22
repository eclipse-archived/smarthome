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
package org.eclipse.smarthome.binding.homematic.internal.communicator.server;

import java.io.IOException;

/**
 * Simple RPC server interface.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcServer {

    /**
     * Starts the rpc server.
     */
    public void start() throws IOException;

    /**
     * Stops the rpc server.
     */
    public void shutdown();

}
