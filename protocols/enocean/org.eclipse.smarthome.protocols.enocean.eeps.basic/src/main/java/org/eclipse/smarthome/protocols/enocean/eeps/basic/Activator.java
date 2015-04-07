/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.eeps.basic;

import org.eclipse.smarthome.protocols.enocean.eeps.basic.utils.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.enocean.descriptions.EnOceanMessageDescription;
import org.osgi.service.enocean.descriptions.EnOceanMessageDescriptionSet;

/**
 * Activator.
 * 
 * Register the three EnOceanMessageDescriptions.
 */
public class Activator implements BundleActivator {

    /** TAG */
    public static final String TAG = Activator.class.getName();

    static final A50205EnOceanMessageDescription A50205EOMD = new A50205EnOceanMessageDescription();
    static final D50001EnOceanMessageDescription D50001EOMD = new D50001EnOceanMessageDescription();
    static final F60201EnOceanMessageDescription F60201EOMD = new F60201EnOceanMessageDescription();

    private BundleContext bc;
    private ServiceRegistration sReg;

    public void start(BundleContext bundleContext) throws InvalidSyntaxException {

        Logger.d(TAG, "IN: org.eclipse.smarthome.protocols.enocean.eeps.basic.Activator.start(bc: " + bundleContext
                + ")");

        this.bc = bundleContext;

        // Register the three ...EnOceanMessageDescription contained in this
        // bundle as an OSGi service (i.e. one service containing a set of the
        // three descriptions);

        EnOceanMessageDescriptionSet enOceanMessageDescriptionSet = new EnOceanMessageDescriptionSet() {
            public EnOceanMessageDescription getMessageDescription(int rorg, int func, int type, int extra)
                    throws IllegalArgumentException {
                // rorg, func, type are int values that represent hexa values.
                if ((rorg == 165) && (func == 2) && (type == 5)) {
                    return A50205EOMD;
                } else if (rorg == 213 && func == 0 && type == 1) {
                    return D50001EOMD;
                } else if (rorg == 246 && func == 2 && type == 1) {
                    return F60201EOMD;
                } else {
                    return null;
                }
            }
        };
        sReg = bc.registerService(EnOceanMessageDescriptionSet.class.getName(), enOceanMessageDescriptionSet, null);

        // Display the EnOceanMessageDescriptionSet services available, and
        // their associated EnOceanMessageDescriptions.
        ServiceReference[] srs = bc.getAllServiceReferences(EnOceanMessageDescriptionSet.class.getName(), null);
        Logger.d(TAG, "srs: " + srs);
        if (srs == null) {
            Logger.d(TAG, "There is NO service registered with the following class name: "
                    + EnOceanMessageDescriptionSet.class.getName());
        } else {
            Logger.d(TAG, "srs.length: " + srs.length);

            int i = 0;
            while (i < srs.length) {
                ServiceReference sRef = srs[i];
                Logger.d(TAG, "sRef: " + sRef);

                String[] pks = sRef.getPropertyKeys();
                int j = 0;
                while (j < pks.length) {
                    Logger.d(
                            TAG,
                            "pks[" + j + "]: " + pks[j] + ", event.getProperty(" + pks[j] + "): "
                                    + sRef.getProperty(pks[j]));
                    j = j + 1;
                }

                EnOceanMessageDescriptionSet eomds = (EnOceanMessageDescriptionSet) bc.getService(sRef);
                Logger.d(TAG, "eomds: " + eomds);
                Logger.d(TAG,
                        "eomds.getMessageDescription(165, 2, 5, -1): " + eomds.getMessageDescription(165, 2, 5, -1));
                Logger.d(TAG,
                        "eomds.getMessageDescription(213, 0, 1, -1): " + eomds.getMessageDescription(213, 0, 1, -1));
                Logger.d(TAG,
                        "eomds.getMessageDescription(246, 2, 1, -1): " + eomds.getMessageDescription(246, 2, 1, -1));
                Logger.d(TAG,
                        "eomds.getMessageDescription(-1, -1, -1, -1): " + eomds.getMessageDescription(-1, -1, -1, -1));

                i = i + 1;
            }
        }

        Logger.d(TAG, "OUT: org.eclipse.smarthome.protocols.enocean.eeps.basic.Activator.start(bc: " + bc + ")");
    }

    public void stop(BundleContext bundleContext) {
        Logger.d(TAG, "IN: org.eclipse.smarthome.protocols.enocean.eeps.basic.Activator.stop(bc: " + bundleContext
                + ")");
        sReg.unregister();
        Logger.d(TAG, "OUT: org.eclipse.smarthome.protocols.enocean.eeps.basic.Activator.stop(bc: " + bc + ")");
    }
}