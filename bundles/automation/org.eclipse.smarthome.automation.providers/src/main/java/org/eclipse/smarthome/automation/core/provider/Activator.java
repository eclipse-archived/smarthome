/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core.provider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.smarthome.automation.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.provider.TemplateProvider;

/**
 * This class is an activator of this bundle. It is responsible for initializing {@link ModuleTypeProvider} and
 * {@link TemplateProvider} and register these services. Also it initialize the Rule Importer to load the rules into
 * Rule Engine.
 * <p>
 * The automation objects come from bundles providing resources in some particular format. So the
 * {@link AutomationResourceBundlesEventQueue} is initialized to track these bundles and it is set to the providers and
 * the importer.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class Activator/* <T extends ModuleTypeProvider, S extends TemplateProvider> */implements BundleActivator {

    private AutomationResourceBundlesEventQueue queue;

    private ServiceRegistration /* <S> */tpReg;
    private ServiceRegistration /* <T> */mtpReg;

    private TemplateResourceBundleProvider tProvider;
    private ModuleTypeResourceBundleProvider mProvider;
    private RuleResourceBundleImporter rImporter;

    /**
     * This method is called when this bundle is started so the Framework can perform the
     * bundle-specific activities as: 
     * <ul> <p>
     * Initializing {@link PersistentModuleTypeResourceBundleProvider},
     * {@link PersistentTemplateResourceBundleProvider}, {@link PersistentRuleResourceBundleImporter} and
     * {@link AutomationResourceBundlesEventQueue} objects. 
     * <p>
     * Registering {@link PersistentModuleTypeResourceBundleProvider},
     * {@link PersistentTemplateResourceBundleProvider} as services, respectively {@link ModuleTypeProvider},
     * {@link TemplateProvider}.
     * <p>
     * Setting to {@link PersistentModuleTypeResourceBundleProvider},
     * {@link PersistentTemplateResourceBundleProvider} and {@link PersistentRuleResourceBundleImporter} the
     * {@link AutomationResourceBundlesEventQueue} object and opens the queue.
     * <p> </ul> 
     * This method must complete and return to its caller in a timely manner.
     * 
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this bundle is
     *         marked as stopped and the Framework will remove this bundle's
     *         listeners, unregister all services registered by this bundle, and
     *         release all services used by this bundle.
     */
    public void start(BundleContext context) throws Exception {

        mProvider = new PersistentModuleTypeResourceBundleProvider(context);
        tProvider = new PersistentTemplateResourceBundleProvider(context);
        rImporter = new PersistentRuleResourceBundleImporter(context);

        queue = new AutomationResourceBundlesEventQueue(context, tProvider, mProvider, rImporter);

        mProvider.setQueue(queue);
        tProvider.setQueue(queue);
        rImporter.setQueue(queue);

        mtpReg = context.registerService(
                new String[] { ModuleTypeProvider.class.getName(), ModuleTypeProvider.class.getName() }, mProvider,
                null);

        tpReg = context.registerService(new String[] { TemplateProvider.class.getName(), TemplateProvider.class.getName() },
                tProvider, null);

        queue.open();
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the {@code BundleActivator.start} method
     * started:
     * <ul> <p>
     * Unregisters {@link PersistentModuleTypeResourceBundleProvider},
     * {@link PersistentTemplateResourceBundleProvider} as services.
     * <p>
     * Stops the queue and closes the providers and importer.
     * <p> </ul> 
     * This method must complete and return to its caller in a timely manner.
     * 
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is still
     *         marked as stopped, and the Framework will remove the bundle's
     *         listeners, unregister all services registered by the bundle, and
     *         release all services used by the bundle.
     */
    public void stop(BundleContext context) throws Exception {
        tpReg.unregister();
        mtpReg.unregister();
        queue.stop();
        tProvider.close();
        mProvider.close();
        rImporter.close();
        tProvider = null;
        mProvider = null;
        rImporter = null;
        tpReg = null;
        mtpReg = null;
    }

}
