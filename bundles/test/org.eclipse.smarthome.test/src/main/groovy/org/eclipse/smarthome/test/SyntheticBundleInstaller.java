/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.config.xml.osgi.AbstractAsyncBundleProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class SyntheticBundleInstaller {

    private static String bundlePoolPath = "/test-bundle-pool";

    public static Bundle install(BundleContext bundleContext, String testBundleName) throws Exception {
        String bundlePath = bundlePoolPath + "/" + testBundleName + "/";
        byte[] syntheticBundleBytes = createSyntheticBundle(bundleContext.getBundle(), bundlePath, testBundleName);

        Bundle syntheticBundle = bundleContext.installBundle(testBundleName, new ByteArrayInputStream(
                syntheticBundleBytes));
        syntheticBundle.start(Bundle.ACTIVE);
        waitUntilLoadingFinished(syntheticBundle);
        return syntheticBundle;
    }

    public static void waitUntilLoadingFinished(Bundle bundle) {
        while (!AbstractAsyncBundleProcessor.isBundleFinishedLoading(bundle)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void uninstall(BundleContext bundleContext, String testBundleName) throws BundleException {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (testBundleName.equals(bundle.getSymbolicName())) {
                bundle.uninstall();
            }
        }
    }

    private static byte[] createSyntheticBundle(Bundle bundle, String bundlePath, String bundleName) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Manifest manifest = getManifest(bundle, bundlePath);
        JarOutputStream jarOutputStream = manifest != null ? new JarOutputStream(outputStream, manifest)
                : new JarOutputStream(outputStream);

        List<String> files = collectFilesFrom(bundle, bundlePath, bundleName);
        for (String file : files) {
            addFileToArchive(bundle, bundlePath, file, jarOutputStream);
        }
        jarOutputStream.close();
        return outputStream.toByteArray();
    }

    private static void addFileToArchive(Bundle bundle, String bundlePath, String fileInBundle,
            JarOutputStream jarOutputStream) throws IOException {
        String filePath = bundlePath + "/" + fileInBundle;
        URL resource = bundle.getResource(filePath);
        if (resource == null)
            return;
        ZipEntry zipEntry = new ZipEntry(fileInBundle);
        jarOutputStream.putNextEntry(zipEntry);
        IOUtils.copy(resource.openStream(), jarOutputStream);
        jarOutputStream.closeEntry();
    }

    private static List<String> collectFilesFrom(Bundle bundle, String bundlePath, String bundleName) throws Exception {
        List<String> result = new ArrayList<>();
        URL url = getBaseURL(bundle, bundleName);
        if (url != null) {
            String path = url.getPath();
            URI baseURI = url.toURI();

            List<URL> list = collectEntries(bundle, path, "*.xml", "*.properties", "*.json");
            for (URL entryURL : list) {
                String fileEntry = convertToFileEntry(baseURI, entryURL);
                result.add(fileEntry);
            }
        }
        return result;
    }

    private static URL getBaseURL(Bundle bundle, String bundleName) {
        Enumeration<URL> entries = bundle.findEntries("/", bundleName, true);
        return entries != null ? entries.nextElement() : null;
    }

    private static List<URL> collectEntries(Bundle bundle, String path, String... filePatterns) {
        List<URL> result = new ArrayList<>();
        for (String filePattern : filePatterns) {
            Enumeration<URL> entries = bundle.findEntries(path, filePattern, true);
            if (entries != null) {
                result.addAll(Collections.list(entries));
            }
        }
        return result;
    }

    private static String convertToFileEntry(URI baseURI, URL entryURL) throws URISyntaxException {
        URI entryURI = entryURL.toURI();
        URI relativeURI = baseURI.relativize(entryURI);
        String fileEntry = relativeURI.toString();
        return fileEntry;
    }

    private static Manifest getManifest(Bundle bundle, String bundlePath) throws IOException {
        String filePath = bundlePath + "/" + "META-INF/MANIFEST.MF";
        URL resource = bundle.getResource(filePath);
        if (resource == null)
            return null;
        return new Manifest(resource.openStream());
    }
}
