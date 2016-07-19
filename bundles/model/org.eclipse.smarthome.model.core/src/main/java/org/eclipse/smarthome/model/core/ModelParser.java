/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core;

/**
 * This interface has to be implemented by services that register an EMF model parser
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ModelParser {

    /**
     * Returns the file extensions of the models this parser registers for.
     *
     * @return file extension of model files
     */
    String getExtension();

}
