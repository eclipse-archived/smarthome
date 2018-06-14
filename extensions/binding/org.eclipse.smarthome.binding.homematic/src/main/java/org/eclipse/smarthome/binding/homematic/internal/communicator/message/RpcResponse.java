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
 * A RPC response definition for reveiving data from the Homematic server.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcResponse {

    /**
     * Returns the decoded methodName.
     */
    public String getMethodName();

    /**
     * Returns the decoded data.
     */
    public Object[] getResponseData();

}
