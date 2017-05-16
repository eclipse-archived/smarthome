/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.java.api.demo;

import org.eclipse.smarthome.automation.java.api.demo.handler.WelcomeHomeHandlerFactory;
import org.eclipse.smarthome.automation.java.api.demo.template.WelcomeHomeTemplateProvider;
import org.eclipse.smarthome.automation.java.api.demo.type.WelcomeHomeModuleTypeProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This class is responsible for starting and stopping the application that gives ability to the user to switch on the
 * air conditioner and lights in its home remotely. It initializes and registers or unregisters the
 * services that provide this functionality - Rule Provider, Rule Template Provider, Module Type Provider and Handler
 * Factory for handlers of the modules that compose the rules. Of course, these providers are not mandatory for each
 * application. Some applications may contain only Template Provider or Rule Provider, or Module Type Provider, or
 * Module Handler Factory for some particular module types. Also, to enable the user to have control over the settings
 * and to enforce execution, the demo initializes and registers a service that provides console commands.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Activator implements BundleActivator {

    private WelcomeHomeModuleTypeProvider mtProvider;
    private WelcomeHomeTemplateProvider tProvider;
    private WelcomeHomeRulesProvider rulesProvider;
    private WelcomeHomeHandlerFactory handlerFactory;
    private WelcomeHomeCommands commands;

    @Override
    public void start(BundleContext context) throws Exception {
        mtProvider = new WelcomeHomeModuleTypeProvider();
        mtProvider.register(context);

        tProvider = new WelcomeHomeTemplateProvider();
        tProvider.register(context);

        rulesProvider = new WelcomeHomeRulesProvider();
        rulesProvider.register(context);

        handlerFactory = new WelcomeHomeHandlerFactory(context);
        handlerFactory.register(context);

        commands = new WelcomeHomeCommands(context, rulesProvider, handlerFactory);
        commands.register(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        mtProvider.unregister();
        mtProvider = null;

        tProvider.unregister();
        tProvider = null;

        commands.unregister();
        commands = null;

        rulesProvider.unregister();
        rulesProvider = null;

        handlerFactory.unregister();
        handlerFactory = null;
    }

}
