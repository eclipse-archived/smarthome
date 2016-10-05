/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.voice.internal;

import java.util.Set;

import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;

/**
 * A DTO that is used on the REST API to provide infos about {@link HumanLanguageInterpreter} to UIs.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class HumanLanguageInterpreterDTO {
    public String id;
    public String label;
    public Set<String> locales;
}
