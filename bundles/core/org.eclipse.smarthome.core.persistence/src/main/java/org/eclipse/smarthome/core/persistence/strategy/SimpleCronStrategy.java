/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.strategy;

/**
 * This class holds a cron expression based strategy to persist items.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class SimpleCronStrategy extends SimpleStrategy {

    private final String cronExpression;

    public SimpleCronStrategy(final String name, final String cronExpression) {
        super(name);
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public String toString() {
        return String.format("%s [%s, cronExpression=%s]", getClass().getSimpleName(), super.toString(),
                cronExpression);
    }

}
