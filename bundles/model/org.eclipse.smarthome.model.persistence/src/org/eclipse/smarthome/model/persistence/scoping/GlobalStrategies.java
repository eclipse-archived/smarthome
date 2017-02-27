/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.persistence.scoping;

import org.eclipse.smarthome.model.persistence.persistence.Strategy;
import org.eclipse.smarthome.model.persistence.persistence.impl.StrategyImpl;

/**
 * This class defines a few persistence strategies that are globally available to
 * all persistence models.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class GlobalStrategies {

    static final public Strategy UPDATE = new StrategyImpl() {
        @Override
        public String getName() {
            return "everyUpdate";
        };
    };

    static final public Strategy CHANGE = new StrategyImpl() {
        @Override
        public String getName() {
            return "everyChange";
        };
    };

    static final public Strategy RESTORE = new StrategyImpl() {
        @Override
        public String getName() {
            return "restoreOnStartup";
        };
    };
}
