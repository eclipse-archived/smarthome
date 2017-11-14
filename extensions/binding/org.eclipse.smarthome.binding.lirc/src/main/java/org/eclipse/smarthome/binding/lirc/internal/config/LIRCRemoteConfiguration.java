/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.config;

/**
 * Configuration class for {@link LIRCRemote} device.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCRemoteConfiguration {

    private String remote;

    /**
     * @return the remote
     */
    public String getRemote() {
        return remote;
    }

    /**
     * @param remote the name of the remote
     */
    public void setRemote(String remote) {
        this.remote = remote;
    }

}
