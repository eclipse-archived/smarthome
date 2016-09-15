/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.internal;

import org.eclipse.smarthome.model.script.engine.action.ActionService;

public class AudioActionService implements ActionService {

    @Override
    public String getActionClassName() {
        return AudioAction.class.getCanonicalName();

    }

    @Override
    public Class<?> getActionClass() {
        return AudioAction.class;
    }

}
