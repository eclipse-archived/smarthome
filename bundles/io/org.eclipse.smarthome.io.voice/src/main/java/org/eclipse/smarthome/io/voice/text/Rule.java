/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

import java.util.ResourceBundle;

/**
 * Represents an expression plus action code that will be executed after successful parsing. This class is immutable and
 * deriving classes should conform to this principle.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public abstract class Rule {
    private Expression expression;

    /**
     * Constructs a new instance.
     *
     * @param expression the expression that has to parse successfully, before {@link interpretAST} is called
     */
    public Rule(Expression expression) {
        this.expression = expression;
    }

    /**
     * Will get called after the expression was successfully parsed.
     *
     * @param language a resource bundle that can be used for looking up common localized response phrases
     * @param node the resulting AST node of the parse run. To be used as input.
     * @return
     */
    public abstract InterpretationResult interpretAST(ResourceBundle language, ASTNode node);

    InterpretationResult execute(ResourceBundle language, TokenList list) {
        ASTNode node = expression.parse(list);
        if (node.isSuccess()) {
            return interpretAST(language, node);
        }
        return InterpretationResult.SYNTAX_ERROR;
    }

    /**
     * @return the expression
     */
    public Expression getExpression() {
        return expression;
    }
}