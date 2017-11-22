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
package org.eclipse.smarthome.model.lazygen;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;

/**
 *
 * @author Holger Schill, Simon Kaufmann - Initial contribution and API
 *
 */
public class GlobalResourceSet {

    public static ResourceSet getINSTANCE() {
        return new XtextResourceSet();
    }
}
