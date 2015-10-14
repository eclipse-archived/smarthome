/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is a {@link BundleActivator} of the gson parser bundle. It is responsible for registration of several
 * services:
 * <li>{@link ModuleType} GSON Parser
 * <li>{@link RuleTemplate} GSON Parser
 * <li>{@link Rule} GSON Parser
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class Activator implements BundleActivator {

    private BundleContext bc;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration mpReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration tpReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration rpReg;

    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;

        Dictionary<String, String> mpReg_props = new Hashtable<String, String>(1);
        mpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_MODULE_TYPE);
        mpReg = bc.registerService(Parser.class.getName(), new ModuleTypeGSONParser(), mpReg_props);

        Dictionary<String, String> tpReg_props = new Hashtable<String, String>(1);
        tpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_TEMPLATE);
        tpReg = bc.registerService(Parser.class.getName(), new TemplateGSONParser(), tpReg_props);

        Dictionary<String, String> rpReg_props = new Hashtable<String, String>(1);
        rpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_RULE);
        rpReg = bc.registerService(Parser.class.getName(), new RuleGSONParser(), rpReg_props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (mpReg != null) {
            mpReg.unregister();
            mpReg = null;
        }
        if (tpReg != null) {
            tpReg.unregister();
            tpReg = null;
        }
        if (rpReg != null) {
            rpReg.unregister();
            rpReg = null;
        }
    }
}
