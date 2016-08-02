/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.karaf.internal;

import org.eclipse.smarthome.io.console.Console;

/**
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class OSGiConsole implements Console {

    private final String scope;

    public OSGiConsole(final String scope) {
        this.scope = scope;
    }

    @Override
    public void print(final String s) {
        System.out.print(s);
    }

    @Override
    public void println(final String s) {
        System.out.println(s);
    }

    @Override
    public void printUsage(final String s) {
        System.out.println(String.format("Usage: %s:%s", scope, s));
    }

}