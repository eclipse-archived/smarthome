package org.eclipse.smarthome.io.net.http.internal;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activate implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        // Nothing to do, the static http client is created on-demand in HttpUtil
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Stop and cleanup http client on bundle stop
        HttpUtil.stopHttpClient();
    }

}
