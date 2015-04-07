/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.eeps.basic.utils;

/**
 * Logger.
 */
public final class Logger {

    static final boolean DEBUG = Boolean.getBoolean("org.osgi.service.enocean.loglevel.debug");
    static final boolean WARN = Boolean.getBoolean("org.osgi.service.enocean.loglevel.warn");
    static final boolean INFO = Boolean.getBoolean("org.osgi.service.enocean.loglevel.info");
    static final boolean ERROR = Boolean.getBoolean("org.osgi.service.enocean.loglevel.error");

    /**
     * DEBUG
     * 
     * @param tag
     * @param msg
     */
    public static final void d(String tag, String msg) {
        if (DEBUG) {
            print("[DEBUG-" + tag + "] " + msg);
        }
    }

    /**
     * INFO
     * 
     * @param tag
     * @param msg
     */
    public static final void i(String tag, String msg) {
        if (INFO) {
            print("[INFO--" + tag + "] " + msg);
        }
    }

    /**
     * WARN
     * 
     * @param tag
     * @param msg
     */
    public static final void w(String tag, String msg) {
        if (WARN) {
            print("[WARN--" + tag + "] " + msg);
        }
    }

    /**
     * ERROR
     * 
     * @param tag
     * @param msg
     */
    public static final void e(String tag, String msg) {
        if (ERROR) {
            print("[ERROR-" + tag + "] " + msg);
        }
    }

    /**
     * Print
     * 
     * @param msg
     */
    private static final void print(String msg) {
        System.out.println(msg);
    }
}
