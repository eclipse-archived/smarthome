/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.osgi;

import org.eclipse.smarthome.config.xml.internal.ConfigDescriptionReader;

/**
 * The {@link XmlDocumentProvider} is responsible managing any created objects
 * by a {@link ConfigDescriptionReader} for a certain module.
 * <p>
 * Each instance of this class is assigned to exactly one module.
 *
 * @author Michael Grammling - Initial Contribution
 *
 * @param <T> the result type of the conversion
 */
public interface XmlDocumentProvider<T> {

    /**
     * Adds a new result object from the XML processing for further processing.
     *
     * @param object the result object to be processed (could be null)
     */
    void addingObject(T object);

    /**
     * Signals that all available result objects from the XML processing of the
     * certain module have been added.
     */
    void addingFinished();

    /**
     * Releases any added result objects from the XML processing.
     */
    void release();

}
