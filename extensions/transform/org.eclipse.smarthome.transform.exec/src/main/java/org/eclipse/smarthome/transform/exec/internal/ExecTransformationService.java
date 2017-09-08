/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.exec.internal;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.net.exec.ExecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by command line.
 *
 * @author Pauli Anttila
 */
public class ExecTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(ExecTransformationService.class);

    /**
     * Transforms the input <code>source</code> by the command line.
     *
     * @param commandLine
     *            the command to execute. Command line should contain %s string,
     *            which will be replaced by the input data.
     * @param source
     *            the input to transform
     */
    @Override
    public String transform(String commandLine, String source) throws TransformationException {

        if (commandLine == null || source == null) {
            throw new TransformationException("the given parameters 'commandLine' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the commanline '{}'", source, commandLine);

        long startTime = System.currentTimeMillis();

        String formattedCommandLine = String.format(commandLine, source);
        String result = ExecUtil.executeCommandLineAndWaitResponse(formattedCommandLine, 5000);
        logger.trace("command line execution elapsed {} ms", System.currentTimeMillis() - startTime);

        return result;
    }

}
