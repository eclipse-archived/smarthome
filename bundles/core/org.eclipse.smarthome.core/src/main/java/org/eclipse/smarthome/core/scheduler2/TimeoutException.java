/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.scheduler2;

/**
 * The singleton exception thrown when a task times out
 * 
 * @author Peter Kriens - initial contribution and API
 */
public class TimeoutException extends Throwable {
    private static final long serialVersionUID = 1L;

    private TimeoutException() {
    }

    /**
     * The singleton timeout exception
     */
    public static TimeoutException SINGLETON = new TimeoutException();

}
