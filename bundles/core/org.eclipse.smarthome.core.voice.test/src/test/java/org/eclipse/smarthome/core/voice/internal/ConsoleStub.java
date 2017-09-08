/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import org.eclipse.smarthome.io.console.Console;

/**
 * A {@link Console} stub used for the tests. In order to keep it as simple as
 * possible, when some text is passed to the console for printing, it is saved
 * in an instance variable. In addition, when the {@link #printUsage(String)}
 * method is called, the corresponding boolean variable is set to true.
 *
 * @author Mihaela Memova - initial contribution
 *
 * @author Velin Yordanov - migrated from groovy to java
 *
 */
public class ConsoleStub implements Console {

    private String printedText;
    private boolean isPrintUsagesMethodCalled;

    @Override
    public void print(String s) {
        printedText = s;
    }

    @Override
    public void println(String s) {
        printedText = s;
    }

    @Override
    public void printUsage(String s) {
        isPrintUsagesMethodCalled = true;
    }

    public String getPrintedText() {
        return printedText;
    }

    public boolean isPrintUsagesMethodCalled() {
        return isPrintUsagesMethodCalled;
    }
}
