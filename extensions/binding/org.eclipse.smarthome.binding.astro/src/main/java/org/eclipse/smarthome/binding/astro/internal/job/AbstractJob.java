/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

/**
 * This class contains the default methods required for different jobs
 *
 * @author Amit Kumar Mondal - Initial contribution
 */
public abstract class AbstractJob implements Job {

    private final String thingUID;

    public AbstractJob(String thingUID) {
        checkArgument(thingUID != null, "The thingUID must not be null");
        this.thingUID = thingUID;
    }

    @Override
    public String getThingUID() {
        return thingUID;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the
     * calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
