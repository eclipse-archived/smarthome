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
 * Expression that successfully parses, if a thing identifier token is found. This class is immutable.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public final class ExpressionIdentifier extends Expression {
    private AbstractRuleBasedInterpreter interpreter;
    private HashSet<String> excludes;

    /**
     * Constructs a new instance.
     *
     * @param interpreter the interpreter it belongs to. Used for dynamically fetching item name tokens
     */
    public ExpressionIdentifier(AbstractRuleBasedInterpreter interpreter) {
        this(interpreter, null);
    }

    /**
     * Constructs a new instance.
     *
     * @param interpreter the interpreter it belongs to. Used for dynamically fetching item name tokens
     * @param excludes tokens that should not occur for this expression to match
     */
    public ExpressionIdentifier(AbstractRuleBasedInterpreter interpreter, HashSet<String> excludes) {
        super();
        this.interpreter = interpreter;
        this.excludes = excludes == null ? new HashSet<String>() : new HashSet<String>(excludes);
    }

    @Override
    ASTNode parse(TokenList list) {
        ASTNode node = new ASTNode();
        HashSet<String> tokens = interpreter.getIdentifierTokens();
        String head = list.head();
        node.setSuccess(tokens.contains(head) && !excludes.contains(head));
        if (node.isSuccess()) {
            node.setRemainingTokens(list.skipHead());
            node.setValue(head);
            node.setChildren(new ASTNode[0]);
            generateValue(node);
        }
        return node;
    }

    @Override
    boolean collectFirsts(HashSet<String> firsts) {
        HashSet<String> f = new HashSet<String>(interpreter.getIdentifierTokens());
        f.removeAll(excludes);
        firsts.addAll(f);
        return true;
    }

    @Override
    public String toString() {
        return "identifier(stop=\"" + excludes + "\")";
    }

    /**
     * @return the interpreter
     */
    public AbstractRuleBasedInterpreter getInterpreter() {
        return interpreter;
    }

    /**
     * @return the excludes
     */
    public HashSet<String> getExcludes() {
        return new HashSet<String>(excludes);
    }
}