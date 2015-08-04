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

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.parser.Parser;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Ana Dimova
 *
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer {

    private BundleContext bc;
    private ServiceReference afRef;
    private AutomationFactory automationFactory;

    private ServiceRegistration /* <Parser> */ mpReg;
    private ServiceRegistration /* <Parser> */ tpReg;
    private ServiceRegistration /* <Parser> */ rpReg;
    private ServiceRegistration converterReg;

    private ServiceTracker automationTracker;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;
        converterReg = bc.registerService(Converter.class.getName(), new ConverterImpl(), null);
        automationTracker = new ServiceTracker(context, AutomationFactory.class.getName(), this);
        automationTracker.open();
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (converterReg != null) {
            converterReg.unregister();
            converterReg = null;
        }
        if (automationTracker != null) {
            automationTracker.close();
            automationTracker = null;
        }
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        Object service = bc.getService(reference);
        if (service instanceof AutomationFactory && afRef == null) {
            afRef = reference;
            automationFactory = (AutomationFactory) service;
        }
        if (automationFactory != null) {
            if (mpReg == null) {
                Dictionary<String, String> props = new Hashtable<String, String>(1);
                props.put(Parser.PARSER_TYPE, Parser.PARSER_MODULE_TYPE);
                mpReg = bc.registerService(Parser.class.getName(), new ModuleTypeJSONParser(bc, automationFactory),
                        props);
            }
            if (tpReg == null) {
                Dictionary<String, String> props = new Hashtable<String, String>(1);
                props.put(Parser.PARSER_TYPE, Parser.PARSER_TEMPLATE);
                tpReg = bc.registerService(Parser.class.getName(), new TemplateJSONParser(automationFactory), props);
            }
            if (rpReg == null) {
                Dictionary<String, String> props = new Hashtable<String, String>(1);
                props.put(Parser.PARSER_TYPE, Parser.PARSER_RULE);
                rpReg = bc.registerService(Parser.class.getName(), new RuleJSONParser(automationFactory), props);
            }
        }
        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        bc.ungetService(reference);
        if (service instanceof AutomationFactory) {
            afRef = null;
            automationFactory = null;
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

}
