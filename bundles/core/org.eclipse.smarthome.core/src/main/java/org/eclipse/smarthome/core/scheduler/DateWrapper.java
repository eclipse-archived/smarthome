/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler;

import java.util.Date;

/**
 * Wraps an instance of Date for testing purposes
 *
 * @author Stefan Triller - initial contribution
 *
 */
public class DateWrapper {

    /**
     * Provides the current date
     *
     * @return the current date
     */
    public Date getDate() {
        return new Date();
    }
}
