/*******************************************************************************
 *
 * Copyright (c) 2017  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/

package org.eclipse.smarthome.automation.handler;

import java.util.Map;

/**
 * {@link TriggerChangeListener} can be used by trigger handlers and to be added to {@link RuleEngineCallback}, to
 * listen for changes of other triggers in the rule.
 *
 * @author Yordan Mihaylov
 */

public interface TriggerChangeListener {

    /**
     * @param triggerUID
     * @param outputs
     */
    public void triggerChanged(String triggerUID, Map<String, ?> outputs);
}
