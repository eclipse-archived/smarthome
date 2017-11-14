/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.test;

import org.eclipse.smarthome.binding.fsinternetradio.handler.FSInternetRadioHandler;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * A mock of FSInternetRadioHandler to enable testing.
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class MockedRadioHandler extends FSInternetRadioHandler {

    public MockedRadioHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean isLinked(String channelUID) {
        return true;
    }
}
