/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap.internal;

import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to the sitemap model files.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SitemapProviderImpl implements SitemapProvider {

	private final Logger logger = LoggerFactory.getLogger(SitemapProviderImpl.class);
	
	private ModelRepository modelRepo = null;
	
	public void setModelRepository(ModelRepository modelRepo) {
		this.modelRepo = modelRepo;
	}
	
	public void unsetModelRepository(ModelRepository modelRepo) {
		this.modelRepo = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.model.sitemap.internal.SitemapProvider#getSitemap(java.lang.String)
	 */
	public Sitemap getSitemap(String sitemapName) {
		if(modelRepo!=null) {
			Sitemap sitemap = (Sitemap) modelRepo.getModel(sitemapName + ".sitemap");
			if(sitemap!=null) {
				return sitemap;
			} else {
				logger.debug("Sitemap {} can not be found", sitemapName);
				return null;
			}
		} else {
			logger.debug("No model repository service is available");
			return null;
		}
	}

}
