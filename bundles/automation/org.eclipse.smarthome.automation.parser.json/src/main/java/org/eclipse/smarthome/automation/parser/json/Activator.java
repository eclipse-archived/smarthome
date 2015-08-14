/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.json;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is a {@link BundleActivator} of the json parser bundle. It is responsible for registration of several
 * services:
 * <li>{@link ModuleType} JSON Parser
 * <li>{@link RuleTemplate} JSON Parser
 * <li>{@link Rule} JSON Parser
 * <li>{@link Converter}
 *
 * @author Ana Dimova
 *
 */
@SuppressWarnings("rawtypes")
public class Activator implements BundleActivator {

    private BundleContext bc;

    private ServiceRegistration mpReg;
    private ServiceRegistration tpReg;
    private ServiceRegistration rpReg;
    private ServiceRegistration converterReg;

    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;
        converterReg = bc.registerService(Converter.class.getName(), new ConverterImpl(), null);

        Dictionary<String, String> mpReg_props = new Hashtable<String, String>(1);
        mpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_MODULE_TYPE);
        mpReg = bc.registerService(Parser.class.getName(), new ModuleTypeJSONParser(bc), mpReg_props);

        Dictionary<String, String> tpReg_props = new Hashtable<String, String>(1);
        tpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_TEMPLATE);
        tpReg = bc.registerService(Parser.class.getName(), new TemplateJSONParser(), tpReg_props);

        Dictionary<String, String> rpReg_props = new Hashtable<String, String>(1);
        rpReg_props.put(Parser.PARSER_TYPE, Parser.PARSER_RULE);
        rpReg = bc.registerService(Parser.class.getName(), new RuleJSONParser(), rpReg_props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (converterReg != null) {
            converterReg.unregister();
            converterReg = null;
        }
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
