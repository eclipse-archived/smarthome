/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

/**
 * @author Alexander Kammerer <alexander.kammerer@online.de>
 *         <p/>
 *         The models implementing this interface act as an additional layer between the data we
 *         parse from the XML files and the concrete use of these models in the template system.
 */
public interface Model<RealImpl> {
    /**
     * @param impl Setter for the model.
     */
    void setModel(RealImpl impl);

    /**
     * @return Returns the {@link RealImpl} object.
     */
    RealImpl getRealImpl();
}
