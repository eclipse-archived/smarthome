/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.internal;

import org.eclipse.smarthome.core.extension.Extension;

/**
 * This is an {@link Extension}, which additionally holds a download url for its content.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MarketplaceExtension extends Extension {

    public MarketplaceExtension(String id, String type, String label, String version, String link, boolean installed,
            String description, String backgroundColor, String imageLink, String downloadUrl) {
        super(id, type, label, version, link, installed, description, backgroundColor, imageLink);
        this.downloadUrl = downloadUrl;
    }

    // we mark it as transient, so that it isn't serialized through GSON
    private transient String downloadUrl;

    /**
     * returns the download url for the content of this extension
     *
     * @return a download url string
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }
}
