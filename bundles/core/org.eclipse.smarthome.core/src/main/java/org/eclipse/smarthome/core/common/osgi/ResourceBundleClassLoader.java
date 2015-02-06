/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * The {@link ResourceBundleClassLoader} is a user defined classloader which is responsible
 * to map files within an <i>OSGi</i> bundle to {@link URL}s. This implementation only supports
 * the method {@link #getResource(String)} for mappings.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ResourceBundleClassLoader extends ClassLoader {

    private Bundle bundle;
    private String path;
    private String filePattern;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bundle the bundle whose files should be mapped (must not be null)
     *
     * @param path the path within the bundle which should be considered to be mapped.
     *            If null is set, all files within the bundle are considered.
     *
     * @param filePattern the pattern for files to be considered within the specified path.
     *            If null is set, all files within the specified path are considered.
     *
     * @throws IllegalArgumentException if the bundle is null
     */
    public ResourceBundleClassLoader(Bundle bundle, String path, String filePattern) throws IllegalArgumentException {

        if (bundle == null) {
            throw new IllegalArgumentException("The bundle must not be null!");
        }

        this.bundle = bundle;
        this.path = (path != null) ? path : "/";
        this.filePattern = (filePattern != null) ? filePattern : "*";
    }

    @Override
    public URL getResource(String name) {
        Enumeration<URL> resourceFiles = this.bundle.findEntries(this.path, this.filePattern, true);

        if (resourceFiles != null) {
            while (resourceFiles.hasMoreElements()) {
                URL resourceURL = resourceFiles.nextElement();
                String resourcePath = resourceURL.getFile();
                File resourceFile = new File(resourcePath);
                String resourceFileName = resourceFile.getName();

                if (resourceFileName.equals(name)) {
                    return resourceURL;
                }
            }
        }

        return null;
    }

}
