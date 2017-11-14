/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;

/**
 * A sitemap event, which provides details about a widget that has changed.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class SitemapWidgetEvent extends SitemapEvent {

    public String widgetId;

    public String label;
    public String icon;
    public String labelcolor;
    public String valuecolor;
    public boolean visibility;
    public EnrichedItemDTO item;

    public SitemapWidgetEvent() {
    }
}