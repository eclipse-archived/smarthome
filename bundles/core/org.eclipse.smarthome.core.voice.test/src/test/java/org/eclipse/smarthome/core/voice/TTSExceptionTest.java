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
 * Test general purpose TTS exception
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class TTSExceptionTest {

    /**
     * Test TTSException() constructor
     */
    @Test
    public void testConstructor0() {
        TTSException ttsException = new TTSException();
        Assert.assertNotNull("TTSException() constructor failed", ttsException);
    }

    /**
     * Test TTSException(String message, Throwable cause) constructor
     */
    @Test
    public void testConstructor1() {
        TTSException ttsException = new TTSException("Message", new Throwable());
        Assert.assertNotNull("TTSException(String, Throwable) constructor failed", ttsException);
    }

    /**
     * Test TTSException(String message) constructor
     */
    @Test
    public void testConstructor2() {
        TTSException ttsException = new TTSException("Message");
        Assert.assertNotNull("TTSException(String) constructor failed", ttsException);
    }

    /**
     * Test TTSException(Throwable cause) constructor
     */
    @Test
    public void testConstructor3() {
        TTSException ttsException = new TTSException(new Throwable());
        Assert.assertNotNull("TTSException(Throwable) constructor failed", ttsException);
    }
}
