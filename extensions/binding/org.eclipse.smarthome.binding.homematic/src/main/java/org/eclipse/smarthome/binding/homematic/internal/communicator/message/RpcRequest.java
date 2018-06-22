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
package org.eclipse.smarthome.binding.homematic.internal.communicator.message;

/**
 * A RPC request definition for sending data to the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcRequest<T> {

    /**
     * Adds arguments to the RPC method.
     */
    public void addArg(Object arg);

    /**
     * Generates the RPC data.
     */
    public T createMessage();

    /**
     * Returns the name of the rpc method.
     */
    public String getMethodName();
}
