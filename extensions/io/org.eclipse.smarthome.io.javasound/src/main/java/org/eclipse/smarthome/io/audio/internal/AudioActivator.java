/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.io.audio.mac.MacAudioSink;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author Karel Goderis - Initial contribution and API
 */
public class AudioActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(AudioActivator.class);

    private static BundleContext context;

    private ServiceRegistration<?> macAudioSinkServiceRegistration = null;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        context = bc;

        logger.debug("Audio Extensions have been started.");

        if (System.getProperty("osgi.os").equals("macosx")) {
            logger.debug("Detected a macOS X host. Adding a special audiosink");
            MacAudioSink macSink = new MacAudioSink();
            macAudioSinkServiceRegistration = context.registerService(AudioSink.class.getName(), macSink,
                    new Hashtable<String, Object>());

        }

    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop(BundleContext bc) throws Exception {

        if (macAudioSinkServiceRegistration != null) {
            macAudioSinkServiceRegistration.unregister();
        }

        context = null;
        logger.debug("Audio Extensions have been stopped.");
    }

    /**
     * Returns the bundle context of this bundle
     *
     * @return the bundle context
     */
    public static BundleContext getContext() {
        return context;
    }
}
