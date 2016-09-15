/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import org.eclipse.smarthome.model.script.engine.action.ActionService;

public class VoiceActionService implements ActionService {

    @Override
    public String getActionClassName() {
        return VoiceAction.class.getCanonicalName();

    }

    @Override
    public Class<?> getActionClass() {
        return VoiceAction.class;
    }
}
