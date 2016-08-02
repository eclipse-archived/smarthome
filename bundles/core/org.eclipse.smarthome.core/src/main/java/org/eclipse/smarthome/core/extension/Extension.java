/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.extension;

/**
 * This class defines an extension.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class Extension {

    private final String id;
    private final String label;
    private final String version;
    private boolean installed;
    private final String type;

    /**
     * Creates a new Extension instance
     *
     * @param id the id of the extension
     * @param type the type id of the extension
     * @param label the label of the extension
     * @param version the version of the extension
     * @param installed true, if the extension is installed, false otherwise
     */
    public Extension(String id, String type, String label, String version, boolean installed) {
        this.id = id;
        this.label = label;
        this.version = version;
        this.installed = installed;
        this.type = type;
    }

    /**
     * The id of the {@ExtensionType} of the extension
     */
    public String getType() {
        return type;
    }

    /**
     * The id of the extension
     */
    public String getId() {
        return id;
    }

    /**
     * The label of the extension
     */
    public String getLabel() {
        return label;
    }

    /**
     * The version of the extension
     */
    public String getVersion() {
        return version;
    }

    /**
     * true, if the extension is installed, false otherwise
     */
    public boolean isInstalled() {
        return installed;
    }

    /**
     * Sets the installed state
     */
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

}
