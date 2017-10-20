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
 * Defines visibility values of {@link Rule}s, {@link ModuleType}s and {@link Template}s
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public enum Visibility {
    /**
     * The UI has always to show an object with such visibility.
     */
    VISIBLE,

    /**
     * The UI has always to hide an object with such visibility.
     */
    HIDDEN,

    /**
     * The UI has to show an object with such visibility only to experts.
     */
    EXPERT

}