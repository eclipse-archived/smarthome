/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Expression that successfully parses, if one of the given alternative expressions matches. This class is immutable.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
final class ExpressionAlternatives extends Expression {
    private List<Expression> subExpressions;

    /**
     * Constructs a new instance.
     *
     * @param subExpressions the sub expressions that are tried/parsed as alternatives in the given order
     */
    public ExpressionAlternatives(Expression... subExpressions) {
        super();
        this.subExpressions = Collections
            .unmodifiableList(Arrays.asList(Arrays.copyOf(subExpressions, subExpressions.length)));
    }

    @Override
    ASTNode parse(TokenList list) {
        ASTNode node = new ASTNode(), cr;
        for (int i = 0; i < subExpressions.size(); i++) {
            cr = subExpressions.get(i).parse(list);
            if (cr.isSuccess()) {
                node.setChildren(new ASTNode[] {
                        cr
                });
                node.setRemainingTokens(cr.getRemainingTokens());
                node.setSuccess(true);
                node.setValue(cr.getValue());
                generateValue(node);
                return node;
            }
        }
        return node;
    }

    @Override
    List<Expression> getChildExpressions() {
        return subExpressions;
    }

    @Override
    boolean collectFirsts(HashSet<String> firsts) {
        boolean blocking = true;
        for (Expression e : subExpressions) {
            blocking = blocking && e.collectFirsts(firsts);
        }
        return blocking;
    }

    @Override
    public String toString() {
        String s = null;
        for (Expression e : subExpressions) {
            s = s == null ? e.toString() : (s + ", " + e.toString());
        }
        return "alt(" + s + ")";
    }
}