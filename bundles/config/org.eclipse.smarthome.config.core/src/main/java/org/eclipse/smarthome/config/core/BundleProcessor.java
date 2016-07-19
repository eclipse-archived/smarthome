/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import org.osgi.framework.Bundle;

/**
 * This interface can be implemented by services that need to process bundles before they should be used by others.
 * The primary purpose is to parse contained declaration files (like thing type XMLs) before thing handlers are
 * initialized.
 *
 * @author Simon Kaufmann - Initial contribution and API
 * @author Kai Kreuzer - refactored and moved to config.core
 */
public interface BundleProcessor {

    /**
     * Check if the loading is finished.
     *
     * @param bundle the bundle the request is for
     * @return true if the loading is finished, false otherwise
     */
    public boolean hasFinishedLoading(Bundle bundle);

    public void registerListener(BundleProcessorListener listener);

    public void unregisterListener(BundleProcessorListener listener);

    public interface BundleProcessorListener {

        public void bundleFinished(BundleProcessor context, Bundle bundle);

    }

}
