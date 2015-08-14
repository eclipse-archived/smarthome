/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.provider;

import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

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
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class Activator<T extends ModuleTypeProvider, S extends TemplateProvider> implements BundleActivator {

    private AutomationResourceBundlesEventQueue queue;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration /* <S> */ tpReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration /* <T> */ mtpReg;

    private TemplateResourceBundleProvider tProvider;
    private ModuleTypeResourceBundleProvider mProvider;
    private RuleResourceBundleImporter rImporter;

    /**
     * This method is called when this bundle is started so the Framework can perform the
     * bundle-specific activities as:
     * <ul>
     * <li>
     * Initializing {@code ModuleTypeResourceBundleProvider}, {@code TemplateResourceBundleProvider},
     * {@code RuleResourceBundleImporter} and {@code  AutomationResourceBundlesEventQueue} objects.
     * <li>
     * Registering {@code ModuleTypeResourceBundleProvider}, {@code TemplateResourceBundleProvider} as services,
     * respectively {@link ModuleTypeProvider}, {@link TemplateProvider}.
     * <li>
     * Setting to {@code ModuleTypeResourceBundleProvider}, {@code TemplateResourceBundleProvider} and
     * {@code RuleResourceBundleImporter} the {@code AutomationResourceBundlesEventQueue} object and opens
     * the queue.
     * <p>
     * </ul>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this bundle is
     *             marked as stopped and the Framework will remove this bundle's
     *             listeners, unregister all services registered by this bundle, and
     *             release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {

        mProvider = new ModuleTypeResourceBundleProvider(context);
        tProvider = new TemplateResourceBundleProvider(context);
        rImporter = new RuleResourceBundleImporter(context);

        queue = new AutomationResourceBundlesEventQueue(context, tProvider, mProvider, rImporter);

        mProvider.setQueue(queue);
        tProvider.setQueue(queue);
        rImporter.setQueue(queue);

        mtpReg = context.registerService(ModuleTypeProvider.class.getName(), mProvider, null);

        tpReg = context.registerService(TemplateProvider.class.getName(), tProvider, null);

    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the {@code BundleActivator.start} method
     * started:
     * <ul>
     * <li>Unregisters {@link PersistentModuleTypeResourceBundleProvider},
     * {@link PersistentTemplateResourceBundleProvider} as services.
     * <li>Stops the queue and closes the providers and importer.
     * </ul>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is still
     *             marked as stopped, and the Framework will remove the bundle's
     *             listeners, unregister all services registered by the bundle, and
     *             release all services used by the bundle.
     */
    @Override
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
