/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap;


public interface SitemapProvider {

    /**
     * This method provides access to sitemap model files, loads them and returns the object model tree.
     * 
     * @param sitemapName the name of the sitemap to load
     * @return the object model tree, null if it is not found
     */
    public Sitemap getSitemap(String sitemapName);

}