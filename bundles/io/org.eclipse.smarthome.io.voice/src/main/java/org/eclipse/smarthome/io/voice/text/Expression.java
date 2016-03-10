/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Base class for all expressions.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public abstract class Expression {
    Expression() {
    }

    abstract ASTNode parse(TokenList list);

    void generateValue(ASTNode node) {
    }

    List<Expression> getChildExpressions() {
        return Collections.emptyList();
    }

    abstract boolean collectFirsts(HashSet<String> firsts);

    HashSet<String> getFirsts() {
        HashSet<String> firsts = new HashSet<String>();
        collectFirsts(firsts);
        return firsts;
    }
}