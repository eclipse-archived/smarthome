/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.osgi;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XmlDocumentBundleTracker} tracks files in the specified XML folder
 * of modules and tries to parse them as XML file with the specified
 * {@link XmlDocumentReader}. Any converted XML files are assigned to its
 * according bundle and added to an {@link XmlDocumentProvider} for further
 * processing. For each module an own {@link XmlDocumentProvider} is created by
 * using the specified {@link XmlDocumentProviderFactory}.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Benedikt Niehues - Changed resource handling so that resources can be
 *         patched by fragments.
 *
 * @param <T>
 *            the result type of the conversion
 */
public class XmlDocumentBundleTracker<T> extends BundleTracker<Bundle> {

    private final Logger logger = LoggerFactory.getLogger(XmlDocumentBundleTracker.class);

    private final String xmlDirectory;

    private final XmlDocumentReader<T> xmlDocumentTypeReader;
    private final XmlDocumentProviderFactory<T> xmlDocumentProviderFactory;

    private final Map<Bundle, XmlDocumentProvider<T>> bundleDocumentProviderMap;

    private final AbstractAsyncBundleProcessor asyncLoader;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bundleContext
     *            the bundle context to be used for tracking bundles (must not
     *            be null)
     * @param xmlDirectory
     *            the directory to search for XML files (must neither be null,
     *            nor empty)
     * @param xmlDocumentTypeReader
     *            the XML converter to be used (must not be null)
     * @param xmlDocumentProviderFactory
     *            the result object processor to be used (must not be null)
     *
     * @throws IllegalArgumentException
     *             if any of the arguments is null
     */
    public XmlDocumentBundleTracker(BundleContext bundleContext, String xmlDirectory,
            XmlDocumentReader<T> xmlDocumentTypeReader, XmlDocumentProviderFactory<T> xmlDocumentProviderFactory)
                    throws IllegalArgumentException {

        super(bundleContext, Bundle.ACTIVE, null);

        if (bundleContext == null) {
            throw new IllegalArgumentException("The BundleContext must not be null!");
        }

        if ((xmlDirectory == null) || (xmlDirectory.isEmpty())) {
            throw new IllegalArgumentException("The XML directory must neither be null, nor empty!");
        }

        if (xmlDocumentTypeReader == null) {
            throw new IllegalArgumentException("The XmlDocumentTypeReader must not be null!");
        }

        if (xmlDocumentProviderFactory == null) {
            throw new IllegalArgumentException("The XmlDocumentProviderFactory must not be null!");
        }

        this.xmlDirectory = xmlDirectory;

        this.xmlDocumentTypeReader = xmlDocumentTypeReader;
        this.xmlDocumentProviderFactory = xmlDocumentProviderFactory;

        this.bundleDocumentProviderMap = new HashMap<>();

        this.asyncLoader = new AbstractAsyncBundleProcessor() {

            @Override
            protected boolean isBundleRelevant(Bundle bundle) {
                return isDirectoryPresent(bundle, XmlDocumentBundleTracker.this.xmlDirectory);
            }

            @Override
            protected void processBundle(Bundle bundle) {
                Enumeration<URL> xmlDocumentPaths = bundle.findEntries(XmlDocumentBundleTracker.this.xmlDirectory,
                        "*.xml", true);
                if (xmlDocumentPaths != null) {
                    Collection<URL> filteredPaths = filterPatches(xmlDocumentPaths, bundle);
                    int numberOfParsedXmlDocuments = 0;
                    for (URL xmlDocumentURL : filteredPaths) {

                        String moduleName = bundle.getSymbolicName();
                        String xmlDocumentFile = xmlDocumentURL.getFile();

                        try {
                            XmlDocumentBundleTracker.this.logger.debug(
                                    "Reading the XML document '{}' in module '{}'...", xmlDocumentFile, moduleName);

                            T object = XmlDocumentBundleTracker.this.xmlDocumentTypeReader.readFromXML(xmlDocumentURL);
                            addingObject(bundle, object);

                            numberOfParsedXmlDocuments++;
                        } catch (Exception ex) {
                            XmlDocumentBundleTracker.this.logger
                                    .warn(String.format("The XML document '%s' in module '%s' could not be parsed: %s",
                                            xmlDocumentFile, moduleName, ex.getLocalizedMessage()), ex);
                        }
                    }

                    if (numberOfParsedXmlDocuments > 0) {
                        addingFinished(bundle);
                    }
                }
            }

        };
    }

    @Override
    public final synchronized void open() {
        super.open();
    }

    @Override
    public final synchronized void close() {
        super.close();
        this.bundleDocumentProviderMap.clear();
    }

    private XmlDocumentProvider<T> acquireXmlDocumentProvider(Bundle bundle) {
        if (bundle != null) {
            XmlDocumentProvider<T> xmlDocumentProvider = this.bundleDocumentProviderMap.get(bundle);

            if (xmlDocumentProvider == null) {
                xmlDocumentProvider = this.xmlDocumentProviderFactory.createDocumentProvider(bundle);

                this.logger.debug("Create an empty XmlDocumentProvider for the module '{}'.", bundle.getSymbolicName());

                this.bundleDocumentProviderMap.put(bundle, xmlDocumentProvider);
            }

            return xmlDocumentProvider;
        }

        return null;
    }

    private void releaseXmlDocumentProvider(Bundle bundle) {
        if (bundle != null) {
            XmlDocumentProvider<T> xmlDocumentProvider = this.bundleDocumentProviderMap.get(bundle);

            if (xmlDocumentProvider != null) {
                try {
                    this.logger.debug("Release the XmlDocumentProvider for the module '{}'.", bundle.getSymbolicName());

                    xmlDocumentProvider.release();
                } catch (Exception ex) {
                    this.logger.error("Could not release the XmlDocumentProvider for the module '"
                            + bundle.getSymbolicName() + "'!", ex);
                }

                this.bundleDocumentProviderMap.remove(bundle);
            }
        }
    }

    private void addingObject(Bundle bundle, T object) {
        XmlDocumentProvider<T> xmlDocumentProvider = acquireXmlDocumentProvider(bundle);

        if (xmlDocumentProvider != null) {
            xmlDocumentProvider.addingObject(object);
        }
    }

    private void addingFinished(Bundle bundle) {
        XmlDocumentProvider<T> xmlDocumentProvider = this.bundleDocumentProviderMap.get(bundle);

        if (xmlDocumentProvider != null) {
            try {
                xmlDocumentProvider.addingFinished();
            } catch (Exception ex) {
                this.logger.error(
                        "Could not send adding finished event for the module '" + bundle.getSymbolicName() + "'!", ex);
            }
        }
    }

    @Override
    public final synchronized Bundle addingBundle(Bundle bundle, BundleEvent event) {
        asyncLoader.addingBundle(bundle);
        return bundle;
    }

    @Override
    public final synchronized void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        this.logger.debug("Removing the XML related objects from module '{}'...", bundle.getSymbolicName());

        asyncLoader.removeBundle(bundle);
        releaseXmlDocumentProvider(bundle);
    }

}
