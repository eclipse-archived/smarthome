/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.text;

/**
 * Bundles results of an interpretation. Represents final outcome and user feedback. This class is immutable.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public final class InterpretationResult {

    /**
     * Represents successful parsing and interpretation.
     */
    public final static InterpretationResult OK = new InterpretationResult(true, "");

    /**
     * Represents a syntactical problem during parsing.
     */
    public final static InterpretationResult SYNTAX_ERROR = new InterpretationResult(false, "Syntax error.");

    /**
     * Represents a problem in the interpretation step after successful parsing.
     */
    public final static InterpretationResult SEMANTIC_ERROR = new InterpretationResult(false, "Semantic error.");

    private boolean success = false;
    private InterpretationException exception;
    private String response;

    /**
     * Constructs a successful result.
     *
     * @param response the textual response. Should be short, localized and understandable by non-technical users.
     */
    public InterpretationResult(String response) {
        super();
        this.response = response;
        this.success = true;
    }

    /**
     * Constructs an unsuccessful result.
     *
     * @param exception the responsible exception
     */
    public InterpretationResult(InterpretationException exception) {
        super();
        this.exception = exception;
        this.success = false;
    }

    /**
     * Constructs a result.
     *
     * @param success if the result represents a successful or unsuccessful interpretation
     * @param response the textual response. Should be short, localized and understandable by non-technical users.
     */
    public InterpretationResult(boolean success, String response) {
        super();
        this.success = success;
        this.response = response;
    }

    /**
     * @return if interpretation was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the exception
     */
    public InterpretationException getException() {
        return exception;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }
}