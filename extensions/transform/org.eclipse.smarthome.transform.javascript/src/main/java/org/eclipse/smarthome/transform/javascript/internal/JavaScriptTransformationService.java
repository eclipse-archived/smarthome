/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.transform.javascript.internal;

import java.util.Objects;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by Java Script.
 *
 * @author Pauli Anttila
 * @author thomask
 */
@NonNullByDefault
@Component(immediate = true, property = { "smarthome.transform=JS" })
public class JavaScriptTransformationService implements TransformationService {

    private Logger logger = LoggerFactory.getLogger(JavaScriptTransformationService.class);
    private @NonNullByDefault({}) JavaScriptEngineManager manager;

    @Reference
    public void setJavaScriptEngineManager(JavaScriptEngineManager manager) {
        this.manager = manager;
    }

    public void unsetJavaScriptEngineManager(JavaScriptEngineManager manager) {
        this.manager = null;
    }

    /**
     * Transforms the input <code>source</code> by Java Script. It expects the
     * transformation rule to be read from a file which is stored under the
     * 'configurations/transform' folder. To organize the various
     * transformations one should use subfolders.
     *
     * @param filename the name of the file which contains the Java script
     *                     transformation rule. Transformation service inject input
     *                     (source) to 'input' variable.
     * @param source   the input to transform
     */
    @Override
    public @Nullable String transform(String filename, String source) throws TransformationException {
        Objects.requireNonNull(filename, "the given parameter 'filename' must not be null");
        Objects.requireNonNull(source, "the given parameter 'source' must not be null");

        final long startTime = System.currentTimeMillis();
        logger.debug("about to transform '{}' by the JavaScript '{}'", source, filename);

        String result = "";

        try {
            final CompiledScript cScript = manager.getScript(filename);
            final Bindings bindings = cScript.getEngine().createBindings();
            bindings.put("input", source);
            result = String.valueOf(cScript.eval(bindings));
            return result;
        } catch (ScriptException e) {
            throw new TransformationException("An error occurred while executing script. " + e.getMessage(), e);
        } finally {
            logger.trace("JavaScript execution elapsed {} ms. Result: {}", System.currentTimeMillis() - startTime,
                    result);
        }
    }
}
