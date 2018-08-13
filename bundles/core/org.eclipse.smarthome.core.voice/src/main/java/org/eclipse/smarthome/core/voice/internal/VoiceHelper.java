/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.voice.internal;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;

/**
 * {@link VoiceHelper} defines a utility method to consume sorted voices.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface VoiceHelper {

    /**
     * Consume sorted voices of a stream of TTS services with a {@link BiConsumer}.
     * <p>
     * The voice sort order depends on the given locale and is:
     * <ol>
     * <li>Voice TTSService label (localized with the given locale)
     * <li>Voice locale (localized with the given locale)
     * <li>Voice label
     * </ol>
     *
     * @param ttsServices the TTS services with voices to consume
     * @param locale the locale used for sorting
     * @param consumer the {@link BiConsumer} with {@link TTSService} and {@link Voice} parameters
     */
    void withSortedVoices(Stream<TTSService> ttsServices, Locale locale, BiConsumer<TTSService, Voice> consumer);
}
