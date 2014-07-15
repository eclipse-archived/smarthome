/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class SyntheticBundleInstaller {

    private static Class<?> clazz = SyntheticBundleInstaller.class;
    private static String bundlePoolPath = "/test-bundle-pool";

    public static Bundle install(BundleContext bundleContext, String testBundleName) throws IOException, BundleException {
        String bundlePath = bundlePoolPath + "/" + testBundleName + "/";
        Path testbundleJar = createSyntheticBundle(bundleContext, bundlePath, testBundleName);

        String location = testbundleJar.toUri().toString();
        Bundle bundle = bundleContext.installBundle(location);
        bundle.start(Bundle.ACTIVE);
        return bundle;
    }
    
    public static void uninstall(BundleContext bundleContext, String testBundleName) throws BundleException {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (testBundleName.equals(bundle.getSymbolicName())) {
                bundle.uninstall();
            }
        }
    }

    private static Path createSyntheticBundle(BundleContext bundleContext, String bundlePath, String bundleName) throws IOException {
        Path testbundleJar = Files.createTempFile(bundleName + "-", ".jar");
        testbundleJar.toFile().deleteOnExit();
        OutputStream outputStream = Files.newOutputStream(testbundleJar, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        List<String> files = collectFilesFrom(bundleContext, bundlePath);
        for (String file : files) {
            addFileToArchive(bundleContext, bundlePath, file, zipOutputStream);
        }
        zipOutputStream.close();
        outputStream.close();
        return testbundleJar;
    }

    private static void addFileToArchive(BundleContext bundleContext, String bundlePath, String fileInBundle, ZipOutputStream zipOutputStream) throws IOException {
        String filePath = bundlePath + "/" + fileInBundle;
        URL resource = bundleContext.getBundle().getResource(filePath);
        if (resource == null)
            return;
        byte[] bytes = IOUtils.toByteArray(resource.openStream());
        ZipEntry zipEntry = new ZipEntry(fileInBundle);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(bytes);
        zipOutputStream.closeEntry();
    }

    private static List<String> collectFilesFrom(BundleContext bundleContext, String resourceFolder) {
        // TODO make dynamic version of collecting resource files
        List<String> result = new ArrayList<>();
        result.add("META-INF/MANIFEST.MF");
        result.add("ESH-INF/binding/binding.xml");
        result.add("ESH-INF/config/config.xml");
        result.add("ESH-INF/thing/thing-types.xml");
        return result;
    }
}
