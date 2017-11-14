/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

/**
 * A general sitemap event, meant to be sub-classed.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class SitemapEvent {

    /** The sitemap name this event is for */
    public String sitemapName;

    /** The page id this event is for */
    public String pageId;

    public SitemapEvent() {
    }
}
