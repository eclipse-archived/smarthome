/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

/**
 * Detailed light information.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Thomas Höfer - added unique id and changed range check for brightness and saturation
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullLight extends Light {
    private State state;
    private String type;
    private String modelid;
    private String swversion;
    private String uniqueid;

    FullLight() {
    }

    /**
     * Returns the current state of the light.
     *
     * @return current state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the type of the light.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the light.
     */
    void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the model ID of the light.
     *
     * @return model id
     */
    public String getModelID() {
        return modelid;
    }

    /**
     * Set the model ID of the light.
     */
    void setModelID(final String modelId) {
        this.modelid = modelId;
    }

    /**
     * Returns the software version of the light.
     *
     * @return software version
     */
    public String getSoftwareVersion() {
        return swversion;
    }

    /**
     * Returns the unique id of the light. The unique is the MAC address of the device with a unique endpoint id in the
     * form: AA:BB:CC:DD:EE:FF:00:11-XX
     *
     * @return the unique id
     */
    public String getUniqueID() {
        return uniqueid;
    }
}
