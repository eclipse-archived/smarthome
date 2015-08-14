/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser;

import java.util.Dictionary;

/**
 * This interfase provides opportunity to use different parsers for converting JsonObject to Dictionary.
 *
 * @author Vasil Ilchev - Initial Contribution
 *
 */
public interface Converter {

    /**
     * Converts String representation of JsonObject to Dictionary.
     *
     * @param source the String representation
     * @return key:value pairs represented by Dictionary.
     */
    @SuppressWarnings("rawtypes")
    public Dictionary getAsDictionary(String source);

}
