/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.parser.ParsingNestedException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * This class can parse and serialize sets of {@link Rule}s.
 *
 * @author Kai Kreuzer - Initial Contribution
 *
 */
public class RuleGSONParser extends AbstractGSONParser<Rule> {

    @Override
    public Set<Rule> parse(InputStreamReader reader) throws ParsingException {
        JsonReader jr = new JsonReader(reader);
        try {
            if (jr.hasNext()) {
                JsonToken token = jr.peek();
                if (JsonToken.BEGIN_ARRAY.equals(token)) {
                    Rule[] rules = gson.fromJson(jr, Rule[].class);
                    return new HashSet<Rule>(Arrays.asList(rules));
                } else {
                    Rule rule = gson.fromJson(jr, Rule.class);
                    Set<Rule> rules = new HashSet<Rule>();
                    rules.add(rule);
                    return rules;
                }
            }
        } catch (Exception e) {
            throw new ParsingException(new ParsingNestedException(ParsingNestedException.RULE, null, e));
        } finally {
            try {
                jr.close();
            } catch (IOException e) {
            }
        }
        return Collections.emptySet();
    }

}
