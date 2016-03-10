/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.HashSet;

/**
 * Expression that successfully parses, if a given string constant is found. This class is immutable.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public final class ExpressionMatch extends Expression {
    private String pattern;

    /**
     * Constructs a new instance.
     *
     * @param pattern the token that has to match for successful parsing
     */
    public ExpressionMatch(String pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    ASTNode parse(TokenList list) {
        ASTNode node = new ASTNode();
        node.setSuccess(list.checkHead(pattern));
        if (node.isSuccess()) {
            node.setRemainingTokens(list.skipHead());
            node.setValue(pattern);
            node.setChildren(new ASTNode[0]);
            generateValue(node);
        }
        return node;
    }

    @Override
    boolean collectFirsts(HashSet<String> firsts) {
        firsts.add(pattern);
        return true;
    }

    @Override
    public String toString() {
        return "match(\"" + pattern + "\")";
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }
}