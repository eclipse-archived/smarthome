/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple;

import org.eclipse.smarthome.automation.Rule;

/**
 * convenience Rule class with a action handler. This allows to define Rules which have a execution block.
 *
 * @author Simon Merschjohann
 *
 */
public abstract class SimpleRule extends Rule implements SimpleRuleActionHandler {

    public SimpleRule() {
        super(null);
    }

}
