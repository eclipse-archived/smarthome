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

import org.eclipse.smarthome.automation.handler.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.handler.provider.TemplateProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Activator/* <T extends ModuleTypeProvider, S extends TemplateProvider> */implements BundleActivator {

    private BundleContext bc;
    private AutomationResourceBundlesEventQueue queue;

    private ServiceRegistration /* <S> */tpReg;
    private ServiceRegistration /* <T> */mtpReg;

    private TemplateResourceBundleProvider tProvider;
    private ModuleTypeResourceBundleProvider mProvider;
    private RuleResourceBundleImporter rProvider;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;

        mProvider = new ModuleTypeResourceBundleProvider(bc);
        tProvider = new TemplateResourceBundleProvider(bc);
        rProvider = new RuleResourceBundleImporter(bc);

        queue = new AutomationResourceBundlesEventQueue(bc, tProvider, mProvider, rProvider);

        mProvider.setQueque(queue);
        tProvider.setQueque(queue);
        rProvider.setQueque(queue);

        mtpReg = bc.registerService(
                new String[] { ModuleTypeProvider.class.getName(), ModuleTypeProvider.class.getName() }, mProvider,
                null);

        tpReg = bc.registerService(new String[] { TemplateProvider.class.getName(), TemplateProvider.class.getName() },
                tProvider, null);

        queue.open();
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        tpReg.unregister();
        mtpReg.unregister();
        queue.stop();
        tProvider.close();
        mProvider.close();
        rProvider.close();

    }

}
