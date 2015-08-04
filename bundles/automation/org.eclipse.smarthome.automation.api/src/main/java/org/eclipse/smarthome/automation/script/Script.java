/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.script;

import java.util.Map;

/**
 * @author Ana Dimova - Initial Contribution
 */
public interface Script<T> {

    public static final String SCRIPT = "script";

    T execute(Map<String, Object> parameters);

    void dispose();
}
