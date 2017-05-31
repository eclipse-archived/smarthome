/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to the sitemap model files.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SitemapProviderImpl implements SitemapProvider {

    protected static final String SITEMAP_FILEEXT = ".sitemap";

    private final Logger logger = LoggerFactory.getLogger(SitemapProviderImpl.class);

    private ModelRepository modelRepo = null;

    public void setModelRepository(ModelRepository modelRepo) {
        this.modelRepo = modelRepo;
    }

    public void unsetModelRepository(ModelRepository modelRepo) {
        this.modelRepo = null;
    }

    @Override
    public Sitemap getSitemap(String sitemapName) {
        if (modelRepo != null) {
            String filename = sitemapName + ".sitemap";
            Sitemap sitemap = (Sitemap) modelRepo.getModel(filename);
            if (sitemap != null) {
                if (!sitemap.getName().equals(sitemapName)) {
                    logger.warn(
                            "Filename `{}` does not match the name `{}` of the sitemap - please fix this as you might see unexpected behavior otherwise.",
                            filename, sitemap.getName());
                }
                return sitemap;
            } else {
                logger.trace("Sitemap {} cannot be found", sitemapName);
                return null;
            }
        } else {
            logger.debug("No model repository service is available");
            return null;
        }
    }

    @Override
    public Set<String> getSitemapNames() {
        Set<String> names = new HashSet<>();
        if (modelRepo != null) {
            for (String name : modelRepo.getAllModelNamesOfType("sitemap")) {
                names.add(StringUtils.removeEnd(name, SITEMAP_FILEEXT));
            }
        } else {
            logger.debug("No model repository service is available");
        }
        return names;
    }

}
