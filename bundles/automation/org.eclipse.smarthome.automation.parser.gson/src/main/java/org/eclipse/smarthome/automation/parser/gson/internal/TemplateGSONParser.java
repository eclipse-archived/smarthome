/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;
import org.eclipse.smarthome.automation.template.Template;

/**
 * This class can parse and serialize sets of {@link Template}s.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class TemplateGSONParser extends AbstractGSONParser<Template> {

    @Override
    public Set<Template> parse(InputStreamReader reader) throws ParsingException {
        try {
            Template[] result = gson.fromJson(reader, Template[].class);
            return new HashSet<Template>(Arrays.asList(result));
        } catch (Exception e) {
            throw new ParsingException(new ParsingNestedException(ParsingNestedException.TEMPLATE, null, e));
        }
    }

}
