/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Base class for all expressions.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public abstract class Expression {

    Expression() {
    }

    abstract ASTNode parse(ResourceBundle language, TokenList list);

    void generateValue(ASTNode node) {
    }

    List<Expression> getChildExpressions() {
        return Collections.emptyList();
    }

    abstract boolean collectFirsts(ResourceBundle language, HashSet<String> firsts);

    HashSet<String> getFirsts(ResourceBundle language) {
        HashSet<String> firsts = new HashSet<String>();
        collectFirsts(language, firsts);
        return firsts;
    }
}