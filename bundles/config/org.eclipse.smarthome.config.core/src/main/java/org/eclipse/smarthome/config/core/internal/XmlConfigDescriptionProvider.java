package org.eclipse.smarthome.config.core.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionListener;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class XmlConfigDescriptionProvider implements ConfigDescriptionProvider {

    private Logger logger = LoggerFactory.getLogger(XmlConfigDescriptionProvider.class.getName());

    @SuppressWarnings("rawtypes")
    class XmlConfigDescriptionBundleTracker extends BundleTracker {

        private static final String CONFIG_DESCRIPTION_SCHEMA_FILE = "org.eclipse.smarthome.config.core.description.schema.xsd";
        private static final String DIRECTORY_ESH_CONFIG = "/ESH-INF/config/";

        @SuppressWarnings("unchecked")
        public XmlConfigDescriptionBundleTracker(BundleContext bundleContext) throws IOException,
                SAXException, JAXBException {
            super(bundleContext, Bundle.ACTIVE, null);
        }

        @Override
        public Object addingBundle(Bundle bundle, BundleEvent event) {
            return null;
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, Object object) {

        }

        private List<URL> getConfigDescriptionFileUrls(Bundle bundle) {
            Enumeration<String> configDescriptionFilePaths = bundle
                    .getEntryPaths(DIRECTORY_ESH_CONFIG);

            List<URL> configDescriptionFileUrls = new ArrayList<>();
            if (configDescriptionFilePaths != null) {

                while (configDescriptionFilePaths.hasMoreElements()) {
                    String configDescriptionFilePath = configDescriptionFilePaths.nextElement();
                    configDescriptionFileUrls.add(bundle.getEntry(configDescriptionFilePath));
                }
            }
            return configDescriptionFileUrls;
        }

        private void parseConfigDescriptions(Bundle bundle, String symbolicName) {
            List<URL> configDescriptionFileUrls = getConfigDescriptionFileUrls(bundle);
            for (URL configDescriptionFileUrl : configDescriptionFileUrls) {

            }
        }
    }

    private List<ConfigDescriptionListener> configDescriptionListeners = new CopyOnWriteArrayList<>();
    private Multimap<String, ConfigDescription> configDescriptions = ArrayListMultimap.create();
    private XmlConfigDescriptionBundleTracker xmlConfigDescriptionBundleTracker;

    @Override
    public void addConfigDescriptionListener(ConfigDescriptionListener listener) {
        synchronized (this) {
            configDescriptionListeners.add(listener);
            notifyListenerAboutAllConfigDescriptionsAdded(listener);
        }
    }

    @Override
    public void removeConfigDescriptionListener(ConfigDescriptionListener listener) {
        synchronized (this) {
            notfiyListenerAboutAllConfigDescriptionsRemoved(listener);
            configDescriptionListeners.remove(listener);
        }
    }

    private void notfiyListenerAboutAllConfigDescriptionsRemoved(ConfigDescriptionListener listener) {
        for (ConfigDescription configDescription : this.configDescriptions.values()) {
            listener.configDescriptionRemoved(configDescription);
        }
    }

    private void notifyAllListenersAboutAddedConfigDescriptions(
            Collection<ConfigDescription> configDescriptions) {
        for (ConfigDescription configDescription : configDescriptions) {
            for (ConfigDescriptionListener configDescriptionListener : configDescriptionListeners) {
                configDescriptionListener.configDescriptionAdded(configDescription);
            }
        }
    }

    private void notifyAllListenersAboutRemovedConfigDescriptions(
            Collection<ConfigDescription> configDescriptions) {
        for (ConfigDescription configDescription : configDescriptions) {
            for (ConfigDescriptionListener configDescriptionListener : configDescriptionListeners) {
                configDescriptionListener.configDescriptionRemoved(configDescription);
            }
        }
    }

    private void notifyListenerAboutAllConfigDescriptionsAdded(ConfigDescriptionListener listener) {
        for (ConfigDescription configDescription : this.configDescriptions.values()) {
            listener.configDescriptionAdded(configDescription);
        }
    }

    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();
        try {
            xmlConfigDescriptionBundleTracker = new XmlConfigDescriptionBundleTracker(bundleContext);
            xmlConfigDescriptionBundleTracker.open();
        } catch (IOException | SAXException | JAXBException ex) {
            logger.error(
                    "Could not initialize config description bundle tracker: " + ex.getMessage(),
                    ex);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        xmlConfigDescriptionBundleTracker.close();
        configDescriptionListeners.clear();
    }

}
