/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
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

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;

/**
 * This class can parse and serialize sets of {@link Rule}s.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class RuleGSONParser extends AbstractGSONParser<Rule> {

    @Override
    public Set<Rule> parse(InputStreamReader reader) throws ParsingException {
        try {
            Rule[] result = gson.fromJson(reader, Rule[].class);
            return new HashSet<Rule>(Arrays.asList(result));
        } catch (Exception e) {
            throw new ParsingException(new ParsingNestedException(ParsingNestedException.RULE, null, e));
        }
    }

}
