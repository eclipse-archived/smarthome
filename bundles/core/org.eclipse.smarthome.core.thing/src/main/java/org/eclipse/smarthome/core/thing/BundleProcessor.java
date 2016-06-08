package org.eclipse.smarthome.core.thing;

import org.osgi.framework.Bundle;

public interface BundleProcessor {

    /**
     * Check if the loading is finished.
     *
     * @param objectFromBundle an object that identifies the bundle the request is for
     * @return null if the loading is finished, a bundle reference if loading is not finished
     */
    public Bundle isFinishedLoading(Object objectFromBundle);

    public void registerListener(BundleProcessorListener listener);

    public void unregisterListener(BundleProcessorListener listener);

    public interface BundleProcessorListener {

        public void bundleFinished(BundleProcessor context, Bundle bundle);

    }

}
