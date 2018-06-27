/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.transform.exec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.net.exec.ExecUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by command line.
 *
 * @author Pauli Anttila
 */
@NonNullByDefault
@Component(immediate = true, property = { "smarthome.transform=EXEC" })
public class ExecTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(ExecTransformationService.class);

    /**
     * Transforms the input <code>source</code> by the command line.
     *
     * @param commandLine the command to execute. Command line should contain %s string,
     *                    which will be replaced by the input data.
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String commandLine, String source) throws TransformationException {
        if (commandLine == null || source == null) {
            throw new TransformationException("the given parameters 'commandLine' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the commandline '{}'", source, commandLine);

        long startTime = System.currentTimeMillis();

        String formattedCommandLine = String.format(commandLine, source);
        String result = ExecUtil.executeCommandLineAndWaitResponse(formattedCommandLine, 5000);
        logger.trace("command line execution elapsed {} ms", System.currentTimeMillis() - startTime);

        return result;
    }

}
