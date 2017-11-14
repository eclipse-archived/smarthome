/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.automation.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.RuleTemplateProvider;
import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a {@link RuleTemplateProvider}, which gets its content from the marketplace extension service
 * and stores it through the ESH storage service.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MarketplaceRuleTemplateProvider extends DefaultAbstractManagedProvider<RuleTemplate, String>
        implements RuleTemplateProvider {

    private final Logger logger = LoggerFactory.getLogger(MarketplaceRuleTemplateProvider.class);

    private Parser<RuleTemplate> parser;

    @Override
    public RuleTemplate getTemplate(String uid, Locale locale) {
        return get(uid);
    }

    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        return getAll();
    }

    @Override
    protected String getStorageName() {
        return "org.eclipse.smarthome.extensionservice.marketplace.RuleTemplates";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    protected void setParser(Parser<RuleTemplate> parser) {
        this.parser = parser;
    }

    protected void unsetParser(Parser<RuleTemplate> parser) {
        this.parser = null;
    }

    /**
     * This adds a new rule template to the persistent storage.
     *
     * @param uid the UID to be used for the template
     * @param json the template content as a json string
     *
     * @throws ParsingException if the content cannot be parsed correctly
     */
    public void addTemplateAsJSON(String uid, String json) throws ParsingException {
        try (InputStreamReader isr = new InputStreamReader(IOUtils.toInputStream(json))) {
            Set<RuleTemplate> templates = parser.parse(isr);
            if (templates.size() != 1) {
                throw new IllegalArgumentException("JSON must contain exactly one template!");
            } else {
                RuleTemplate entry = templates.iterator().next();
                RuleTemplate template = new RuleTemplate(uid, entry.getLabel(), entry.getDescription(), entry.getTags(),
                        entry.getTriggers(), entry.getConditions(), entry.getActions(),
                        entry.getConfigurationDescriptions(), entry.getVisibility());
                add(template);
            }
        } catch (IOException e) {
            logger.error("Cannot close input stream.", e);
        }
    }

}
