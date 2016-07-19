/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.voice.mactts.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Test MacTTSVoice
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class MacTTSVoiceTest {

    /**
     * Test MacTTSVoice(String) constructor
     */
    @Test
    public void testConstructor() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice(String) constructor failed", voiceMacOS);
        } catch (IOException e) {
            Assert.fail("testConstructor() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test VoiceMacOS.getUID()
     */
    @Test
    public void getUIDTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice macTTSVoice = new MacTTSVoice(nextLine);
            Assert.assertTrue("The VoiceMacOS UID has an incorrect format",
                    (0 == macTTSVoice.getUID().indexOf("mactts:")));
        } catch (IOException e) {
            Assert.fail("getUIDTest() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test MacTTSVoice.getLabel()
     */
    @Test
    public void getLabelTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice label has an incorrect format", voiceMacOS.getLabel());
        } catch (IOException e) {
            Assert.fail("getLabelTest() failed with IOException: " + e.getMessage());
        }
    }

    /**
     * Test MacTTSVoice.getLocale()
     */
    @Test
    public void getLocaleTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));

        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice locale has an incorrect format", voiceMacOS.getLocale());
        } catch (IOException e) {
            Assert.fail("getLocaleTest() failed with IOException: " + e.getMessage());
        }
    }
}
