/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.multimedia.internal.tts;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.smarthome.io.multimedia.tts.TTSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for MacOS, which simply uses the "say" command from MacOS.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Pauli Anttila
 *
 */
public class TTSServiceMacOS implements TTSService {

    private final Logger logger = LoggerFactory.getLogger(TTSServiceMacOS.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void say(String text, String voiceName, String outputDevice) {

        ArrayList<String> list = new ArrayList<String>();
        list.add("say");

        if (outputDevice != null) {
            list.add("-a");
            list.add(outputDevice);
        }
        if (voiceName != null) {
            list.add("-v");
            list.add(voiceName);
        }

        list.add(text.replace("-", " minus "));

        try {
            Process process = Runtime.getRuntime().exec(list.toArray(new String[list.size()]));
            process.waitFor();
        } catch (IOException e) {
            logger.error("Error while executing the 'say' command: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("The 'say' command has been interrupted: " + e.getMessage());
        }
    }
}
