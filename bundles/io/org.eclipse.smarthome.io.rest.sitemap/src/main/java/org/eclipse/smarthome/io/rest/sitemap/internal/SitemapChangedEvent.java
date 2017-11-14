/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

/**
 * Event to notify the browser that the sitemap has been changed
 *
 * @author Stefan Triller - Initial Contribution
 *
 */
public class SitemapChangedEvent extends SitemapEvent {
    public final String TYPE = "SITEMAP_CHANGED";
}
