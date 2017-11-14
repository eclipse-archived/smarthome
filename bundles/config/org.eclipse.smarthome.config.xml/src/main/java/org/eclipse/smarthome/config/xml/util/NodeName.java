/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.util;

/**
 * The {@link NodeName} interface defines common features for all {@code Node}* classes.
 * <p>
 * Each {@code Node}* class has to return its node name.
 *
 * @author Michael Grammling - Initial Contribution
 */
public interface NodeName {

    /**
     * Returns the name of the node this object belongs to.
     *
     * @return the name of the node this object belongs to (neither null, nor empty)
     */
    String getNodeName();

}
