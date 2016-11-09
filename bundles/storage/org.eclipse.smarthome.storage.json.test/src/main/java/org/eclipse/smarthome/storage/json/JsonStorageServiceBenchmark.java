/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json;

import java.io.File;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.junit.Test;

/**
 * Quick and dirty benchmark test
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class JsonStorageServiceBenchmark {

    @Test
    public void benchmark1() {
        final int TotalRuns = 2000;
        JsonStorageService service = new JsonStorageService();
        service.activate();

        JsonStorage<Object> storage = (JsonStorage) service.getStorage("TestStorage", this.getClass().getClassLoader());

        Item item;

        long start = System.currentTimeMillis();
        for (int x = 0; x < TotalRuns; x++) {
            item = new NumberItem("Item " + x);
            storage.put("item " + x + 1, item);
        }
        storage.commitDatabase();
        long duration = System.currentTimeMillis() - start;
        System.out.println("Json storage insert: " + TotalRuns + " runs in " + duration + "ms");

        File sizing = new File(ConfigConstants.getUserDataFolder() + File.separator + "jsondb" + File.separator);
        System.out.println("Json storage size: " + getFolderSize(sizing) + " bytes");

        // clean up database files ...
        deleteDir(new File(ConfigConstants.getUserDataFolder()));
    }

    @Test
    public void benchmark2() {
        final int TotalRuns = 2000;
        JsonStorageService service = new JsonStorageService();
        service.activate();

        JsonStorage<Object> storage = (JsonStorage) service.getStorage("TestStorage", this.getClass().getClassLoader());

        Item item;

        long start = System.currentTimeMillis();
        for (int x = 0; x < TotalRuns; x++) {
            item = new NumberItem("Item " + x);
            storage.put("item " + x + 1, item);
        }
        storage.commitDatabase();
        long duration = System.currentTimeMillis() - start;
        System.out.println("Json storage insert: " + TotalRuns + " runs in " + duration + "ms");

        start = System.currentTimeMillis();
        for (int x = 0; x < TotalRuns; x++) {
            item = new NumberItem("Item " + x + " updated");
            storage.put("item " + x + 1, item);
        }
        storage.commitDatabase();
        duration = System.currentTimeMillis() - start;
        System.out.println("Json storage insert: " + TotalRuns + " runs in " + duration + "ms");

        File sizing = new File(ConfigConstants.getUserDataFolder() + File.separator + "jsondb" + File.separator);
        System.out.println("Json storage size: " + getFolderSize(sizing) + " bytes");

        // clean up database files ...
        deleteDir(new File(ConfigConstants.getUserDataFolder()));
    }

    @Test
    public void benchmark3() {
        final int TotalRuns = 20000;
        JsonStorageService service = new JsonStorageService();
        service.activate();

        JsonStorage<Object> storage = (JsonStorage) service.getStorage("TestStorage", this.getClass().getClassLoader());

        Item item;

        long start = System.currentTimeMillis();
        for (int x = 0; x < TotalRuns; x++) {
            item = new NumberItem("Item " + x);
            storage.put("item " + x + 1, item);
        }
        storage.commitDatabase();
        long duration = System.currentTimeMillis() - start;
        System.out.println("Json storage insert: " + TotalRuns + " runs in " + duration + "ms");

        File sizing = new File(ConfigConstants.getUserDataFolder() + File.separator + "jsondb" + File.separator);
        System.out.println("Json storage size: " + getFolderSize(sizing) + " bytes");

        // clean up database files ...
        deleteDir(new File(ConfigConstants.getUserDataFolder()));
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty or this is a file so delete it
        return dir.delete();
    }

}
