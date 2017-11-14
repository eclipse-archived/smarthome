/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import java.util.Locale;

import org.eclipse.smarthome.core.voice.Voice;

/**
 * A {@link Voice} stub used for the tests.
 *
 * @author Mihaela Memova - initial contribution
 *
 * @author Velin Yordanov - migrated from groovy to java
 *
 */
public class VoiceStub implements Voice {

    private TTSServiceStub ttsService = new TTSServiceStub();

    private final String VOICE_STUB_ID = "voiceStubID";
    private final String VOICE_STUB_LABEL = "voiceStubLabel";

    @Override
    public String getUID() {
        return ttsService.getId() + ":" + VOICE_STUB_ID;
    }

    @Override
    public String getLabel() {
        return VOICE_STUB_LABEL;
    }

    @Override
    public Locale getLocale() {
        // we need to return something different from null here (the real value is not important)
        return Locale.getDefault();
    }
}
