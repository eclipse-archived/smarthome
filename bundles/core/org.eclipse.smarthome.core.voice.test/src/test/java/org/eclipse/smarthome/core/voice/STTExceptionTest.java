/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test general purpose STT exception
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class STTExceptionTest {

    /**
     * Test STTException() constructor
     */
    @Test
    public void testConstructor0() {
        STTException ttsException = new STTException();
        Assert.assertNotNull("STTException() constructor failed", ttsException);
    }

    /**
     * Test STTException(String message, Throwable cause) constructor
     */
    @Test
    public void testConstructor1() {
        STTException ttsException = new STTException("Message", new Throwable());
        Assert.assertNotNull("STTException(String, Throwable) constructor failed", ttsException);
    }

    /**
     * Test STTException(String message) constructor
     */
    @Test
    public void testConstructor2() {
        STTException ttsException = new STTException("Message");
        Assert.assertNotNull("STTException(String) constructor failed", ttsException);
    }

    /**
     * Test STTException(Throwable cause) constructor
     */
    @Test
    public void testConstructor3() {
        STTException ttsException = new STTException(new Throwable());
        Assert.assertNotNull("STTException(Throwable) constructor failed", ttsException);
    }
}
