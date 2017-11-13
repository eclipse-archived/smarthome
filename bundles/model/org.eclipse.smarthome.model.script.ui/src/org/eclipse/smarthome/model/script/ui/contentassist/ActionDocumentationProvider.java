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
package org.eclipse.smarthome.model.script.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.ui.hover.XbaseHoverDocumentationProvider;

public class ActionDocumentationProvider extends XbaseHoverDocumentationProvider {

    @Override
    public String getDocumentation(EObject o) {
        String doc = super.getDocumentation(o);
        return doc;
    }

}
