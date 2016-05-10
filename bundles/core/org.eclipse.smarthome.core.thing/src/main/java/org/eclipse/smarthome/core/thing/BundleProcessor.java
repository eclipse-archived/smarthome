package org.eclipse.smarthome.core.thing;

import org.osgi.framework.Bundle;

public interface BundleProcessor {

    public Bundle isFinishedLoading(Object objectFromBundle);

    public void registerListener(BundleProcessorListener listener);

    public void unregisterListener(BundleProcessorListener listener);

    public interface BundleProcessorListener {

        public void bundleFinished(BundleProcessor context, Bundle bundle);

    }

}
