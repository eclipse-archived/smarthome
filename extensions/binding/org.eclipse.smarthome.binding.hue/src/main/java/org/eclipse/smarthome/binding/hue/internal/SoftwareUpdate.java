/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

/**
 * Details of a bridge firmware update.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class SoftwareUpdate {
    private int updatestate;
    private String url;
    private String text;
    private boolean notify;

    /**
     * Returns the state of the update.
     * TODO: Actual meaning currently undocumented
     *
     * @return state of update
     */
    public int getUpdateState() {
        return updatestate;
    }

    /**
     * Returns the url of the changelog.
     *
     * @return changelog url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns a description of the update.
     *
     * @return update description
     */
    public String getText() {
        return text;
    }

    /**
     * Returns if there will be a notification about this update.
     *
     * @return true for notification, false otherwise
     */
    public boolean isNotified() {
        return notify;
    }
}
