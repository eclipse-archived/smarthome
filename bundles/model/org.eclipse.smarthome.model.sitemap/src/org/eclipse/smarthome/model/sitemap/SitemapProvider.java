/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.model.sitemap;

import java.util.Set;


public interface SitemapProvider {

    /**
     * This method provides access to sitemap model files, loads them and returns the object model tree.
     * 
     * @param sitemapName the name of the sitemap to load
     * @return the object model tree, null if it is not found
     */
    public Sitemap getSitemap(String sitemapName);

    /**
     * Returns the names of all available sitemaps
     * 
     * @return names of provided sitemaps
     */
    public Set<String> getSitemapNames();
}