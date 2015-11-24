/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * Defines visibility values of {@link ModuleType}s and {@link Template}s
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public enum Visibility {
    /**
     * The UI has to show this object.
     */
    VISIBLE,

    /**
     * The UI has to hide this object.
     */
    HIDDEN,

    /**
     * The UI has to show this object only to experts.
     */
    EXPERT

}