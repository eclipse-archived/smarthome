/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.automation;

/**
 * Marker interface for RuleActions
 *
 * Every method in the implementation should provide annotations which are used to create the ModuleTypes
 *
 * @author Stefan Triller - initial contribution
 *
 */
public interface AnnotatedActions {

    /**
     * This constant is used as a parameter on OSGi services. It is a marker for the framework to recognize
     * which @{@link ThingHandler} instance to use. The handler is retrieved using this {@link ThingUID}.
     */
    static final String ACTION_THING_UID = "esh.thingActionID";
}
