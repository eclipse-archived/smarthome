/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.automation.internal;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.automation.template.RuleTemplateProvider;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtension;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtensionHandler;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MarketplaceExtensionHandler} implementation, which handles rule templates as JSON files and installs
 * them by adding them to a {@link Storage}. The templates are then served from this storage through a dedicated
 * {@link RuleTemplateProvider}.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class AutomationExtensionHandler implements MarketplaceExtensionHandler {

    private final Logger logger = LoggerFactory.getLogger(AutomationExtensionHandler.class);

    private MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider;

    protected void setMarketplaceRuleTemplateProvider(MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider) {
        this.marketplaceRuleTemplateProvider = marketplaceRuleTemplateProvider;
    }

    protected void unsetMarketplaceRuleTemplateProvider(
            MarketplaceRuleTemplateProvider marketplaceRuleTemplateProvider) {
        this.marketplaceRuleTemplateProvider = null;
    }

    @Override
    public boolean supports(MarketplaceExtension ext) {
        // we support only rule templates in JSON format so far
        return ext.getType().equals(MarketplaceExtension.EXT_TYPE_RULE_TEMPLATE)
                && ext.getPackageFormat().equals(MarketplaceExtension.EXT_FORMAT_JSON);
    }

    @Override
    public boolean isInstalled(MarketplaceExtension ext) {
        return marketplaceRuleTemplateProvider.get(ext.getId()) != null;
    }

    @Override
    public void install(MarketplaceExtension ext) throws MarketplaceHandlerException {
        String url = ext.getDownloadUrl();
        try {
            String template = getTemplate(url);
            marketplaceRuleTemplateProvider.addTemplateAsJSON(ext.getId(), template);
        } catch (IOException e) {
            logger.error("Rule template from marketplace cannot be downloaded: {}", e.getMessage());
            throw new MarketplaceHandlerException("Template cannot be downloaded.");
        } catch (Exception e) {
            logger.error("Rule template from marketplace is invalid: {}", e.getMessage());
            throw new MarketplaceHandlerException("Template is not valid.");
        }
    }

    @Override
    public void uninstall(MarketplaceExtension ext) throws MarketplaceHandlerException {
        marketplaceRuleTemplateProvider.remove(ext.getId());
    }

    private String getTemplate(String urlString) throws IOException {
        URL url = new URL(urlString);
        return IOUtils.toString(url);
    }

}
