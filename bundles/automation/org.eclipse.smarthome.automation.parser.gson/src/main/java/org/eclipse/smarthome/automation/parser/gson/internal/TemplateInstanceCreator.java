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
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.lang.reflect.Type;

import org.eclipse.smarthome.automation.template.RuleTemplate;

import com.google.gson.InstanceCreator;

/**
 * This class creates {@link RuleTemplate} instances.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateInstanceCreator implements InstanceCreator<RuleTemplate> {

    @Override
    public RuleTemplate createInstance(Type type) {
        return new RuleTemplate(null, null, null, null, null, null, null, null, null);
    }
}
