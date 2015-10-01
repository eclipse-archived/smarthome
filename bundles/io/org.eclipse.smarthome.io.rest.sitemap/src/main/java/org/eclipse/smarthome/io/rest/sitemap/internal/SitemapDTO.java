/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

/**
 * This is a data transfer object that is used to serialize sitemaps.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 *
 */
public class SitemapDTO {

    public String name;
    public String icon;
    public String label;

    public String link;

    public PageDTO homepage;

}
