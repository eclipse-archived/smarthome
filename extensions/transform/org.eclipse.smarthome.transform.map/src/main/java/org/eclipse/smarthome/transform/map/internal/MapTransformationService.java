/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.map.internal;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.core.transform.AbstractFileTransformationService;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which simply maps strings to other strings
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Gaël L'hopital - Make it localizable
 */
public class MapTransformationService extends AbstractFileTransformationService<Properties> {

    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);

    /**
     * <p>
     * Transforms the input <code>source</code> by mapping it to another string. It expects the mappings to be read from
     * a file which is stored under the 'configurations/transform' folder. This file should be in property syntax, i.e.
     * simple lines with "key=value" pairs. To organize the various transformations one might use subfolders.
     * </p>
     *
     * @param properties
     *            the list of properties which contains the key value pairs for the mapping.
     * @param source
     *            the input to transform
     *
     * @{inheritDoc
     *
     */
    @Override
    protected String internalTransform(Properties properties, String source) throws TransformationException {
        String target = properties.getProperty(source);
        if (target != null) {
            logger.debug("transformation resulted in '{}'", target);
        }
        return target;
    }

    @Override
    protected Properties internalLoadTransform(String filename) throws TransformationException {
        try {
            Properties result = new Properties();
            result.load(new FileReader(filename));
            return result;
        } catch (IOException e) {
            throw new TransformationException("An error occured while opening file.", e);
        }
    }

}
