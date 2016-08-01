/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.model.core.ModelParser;
import org.eclipse.smarthome.model.rule.RulesStandaloneSetup;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class RuleRuntimeActivator implements ModelParser {

    private final Logger logger = LoggerFactory.getLogger(RuleRuntimeActivator.class);

    public void activate(BundleContext bc) throws Exception {
        RulesStandaloneSetup.doSetup();
        logger.debug("Registered 'rule' configuration parser");
    }

    public void deactivate() throws Exception {
    }

    @Override
    public String getExtension() {
        return "rules";
    }

}
