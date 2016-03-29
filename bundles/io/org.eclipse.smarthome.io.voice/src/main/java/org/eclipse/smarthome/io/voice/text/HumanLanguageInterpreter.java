/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.Locale;
import java.util.Set;

/**
 * This is the interface that a human language text interpreter has to implement.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public interface HumanLanguageInterpreter {

    /**
     * Interprets a human language text fragment of a given {@link Locale}
     *
     * @param locale language of the text (given by a {@link Locale})
     * @param text the text to interpret
     * @return a human language response
     */
    String interpret(Locale locale, String text) throws InterpretationException;

    /**
     * Gets all supported languages of the interpreter by their {@link Locale}s
     *
     * @return Set of supported languages (each given by a {@link Locale})
     */
    Set<Locale> getSupportedLocales();

}
