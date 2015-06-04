/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class TemplateManagerImpl implements TemplateManager {

    private TemplateRegistry templateRegistry;

    /**
   *
   */
    public TemplateManagerImpl(TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    @Override
    public Template getTemplate(String templateUID) {
        return templateRegistry.get(templateUID, null);
    }

}
