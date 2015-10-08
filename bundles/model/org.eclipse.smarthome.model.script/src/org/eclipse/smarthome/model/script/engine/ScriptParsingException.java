/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.engine;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.diagnostics.AbstractDiagnostic;
import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
import org.eclipse.xtext.validation.Issue;

/**
 * Exception that is thrown on errors during script execution.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ScriptParsingException extends ScriptException {

    private static final long serialVersionUID = -3784970293118871807L;

    public ScriptParsingException(String message, String scriptAsString) {
        super(message, scriptAsString);
    }

    public ScriptParsingException(String message, String scriptAsString, Throwable t) {
        super(message, scriptAsString, t);
    }

    public ScriptParsingException addDiagnosticErrors(List<Diagnostic> errors) {
        for (Diagnostic emfDiagnosticError : errors) {
            if (emfDiagnosticError instanceof AbstractDiagnostic) {
                AbstractDiagnostic e = (AbstractDiagnostic) emfDiagnosticError;
                this.getErrors().add(new ScriptError(e.getMessage(), e.getLine(), e.getOffset(), e.getLength()));
            } else if (emfDiagnosticError instanceof ExceptionDiagnostic) {
                ExceptionDiagnostic e = (ExceptionDiagnostic) emfDiagnosticError;
                this.getErrors().add(new ScriptError(e.getMessage(), e.getLine(), e.getOffset(), e.getLength()));
            } else {
                this.getErrors().add(new ScriptError(emfDiagnosticError.getMessage(), -1, -1, -1));
            }
        }
        return this;
    }

    public ScriptParsingException addValidationIssues(Iterable<Issue> validationErrors) {
        for (Issue validationError : validationErrors) {
            this.getErrors().add(
                    new ScriptError(validationError.getMessage(), validationError.getLineNumber(), validationError
                            .getOffset(), validationError.getLength()));
        }
        return this;
    }

}
