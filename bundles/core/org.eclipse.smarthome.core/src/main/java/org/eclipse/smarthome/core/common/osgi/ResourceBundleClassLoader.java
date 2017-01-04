/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

/**
 * The {@link ResourceBundleClassLoader} is a user defined classloader which is
 * responsible to map files within an <i>OSGi</i> bundle to {@link URL}s. This
 * implementation only supports the method {@link #getResource(String)} for
 * mappings.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Martin Herbst - ISO-8859-1 support added
 */
public class ResourceBundleClassLoader extends ClassLoader {

    /**
     * ISO-8859-1 must be the last array element because all files could be decoded to ISO-8859-1 and
     * therefore always character set ISO-8859-1 would be assumed.
     */
    private static final Charset[] SUPPORTED_CHARSETS = { Charset.forName("UTF-8"), Charset.forName("ISO-8859-1") };

    private Bundle bundle;
    private String path;
    private String filePattern;
    private Map<URL, Charset> resourceNamesEncoding;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bundle
     *            the bundle whose files should be mapped (must not be null)
     *
     * @param path
     *            the path within the bundle which should be considered to be
     *            mapped. If null is set, all files within the bundle are
     *            considered.
     *
     * @param filePattern
     *            the pattern for files to be considered within the specified
     *            path. If null is set, all files within the specified path are
     *            considered.
     *
     * @throws IllegalArgumentException
     *             if the bundle is null
     */
    public ResourceBundleClassLoader(Bundle bundle, String path, String filePattern) throws IllegalArgumentException {

        if (bundle == null) {
            throw new IllegalArgumentException("The bundle must not be null!");
        }

        this.bundle = bundle;
        this.path = (path != null) ? path : "/";
        this.filePattern = (filePattern != null) ? filePattern : "*";
        this.resourceNamesEncoding = determineResourceEncoding();
    }

    /**
     * Special constructor needed to access method {@link #isCharsetValid(InputStream, Charset)} without a valid OSGi
     * bundle
     */
    ResourceBundleClassLoader() {
        super();
    }

    @Override
    public URL getResource(String name) {
        Enumeration<URL> resourceFiles = this.bundle.findEntries(this.path, this.filePattern, true);

        List<URL> allResources = new LinkedList<URL>();
        if (resourceFiles != null) {
            while (resourceFiles.hasMoreElements()) {
                URL resourceURL = resourceFiles.nextElement();
                String resourcePath = resourceURL.getFile();
                File resourceFile = new File(resourcePath);
                String resourceFileName = resourceFile.getName();

                if (resourceFileName.equals(name)) {
                    allResources.add(resourceURL);
                }
            }
        }

        if (allResources.isEmpty()) {
            return null;
        }

        if (allResources.size() == 1) {
            return allResources.get(0);
        }

        // handle fragment resources. return first one.
        for (URL url : allResources) {
            boolean isHostResource = bundle.getEntry(url.getPath()) != null
                    && bundle.getEntry(url.getPath()).equals(url);
            if (isHostResource) {
                continue;
            }
            return url;
        }

        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL resourceURL = getResource(name);
        if (resourceURL != null) {
            try {
                Charset charset = resourceNamesEncoding.get(resourceURL);
                InputStream resourceStream = null;
                if (charset != null) {
                    resourceStream = resourceURL.openStream();
                }
                if (resourceStream != null) {
                    try (Reader resourceReader = new InputStreamReader(resourceStream, charset)) {
                        Properties props = new Properties();
                        props.load(resourceReader);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        props.store(baos, "converted");
                        return new ByteArrayInputStream(baos.toByteArray());
                    }
                }
            } catch (IOException e) {
            }
            return super.getResourceAsStream(name);
        }
        return null;
    }

    private Map<URL, Charset> determineResourceEncoding() {
        Map<URL, Charset> resourceNamesEncoding = new HashMap<URL, Charset>();

        Enumeration<URL> resourceFiles = this.bundle.findEntries(this.path, this.filePattern, true);

        if (resourceFiles != null) {
            while (resourceFiles.hasMoreElements()) {
                URL resourceURL = resourceFiles.nextElement();
                Charset charset = getResourceCharset(resourceURL);
                resourceNamesEncoding.put(resourceURL, charset);
            }
        }

        return resourceNamesEncoding;
    }

    /**
     * The method tries to find out the encoding (character set) of the passed file.
     * This is done by testing the character sets from {@link #SUPPORTED_CHARSETS}
     *
     * @param url
     *            URL of the resource that should be checked
     * @return
     *         identified character set or null if the file can't be decoded with any
     *         of the given character sets
     */
    private Charset getResourceCharset(URL url) {
        String path = url.getFile();
        File resourceFile = new File(path);
        String name = resourceFile.getName();
        try (InputStream res = super.getResourceAsStream(name)) {
            byte[] content = IOUtils.toByteArray(res);
            for (Charset charset : SUPPORTED_CHARSETS) {
                if (isCharsetValid(content, charset)) {
                    return charset;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * The method tests whether the file content can be decoded with the given character set.
     *
     * @param content
     *            resource content as byte array
     * @param charset
     *            character set to which the file is tested
     * @return
     *         true if the file content could be decoded with the given character set, otherwise false
     */
    boolean isCharsetValid(byte[] content, Charset charset) {
        try {
            charset.newDecoder().decode(ByteBuffer.wrap(content));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
