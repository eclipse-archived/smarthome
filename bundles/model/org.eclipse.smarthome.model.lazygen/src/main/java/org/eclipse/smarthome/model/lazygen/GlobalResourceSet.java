/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private static ResourceSet INSTANCE = null;

	public static ResourceSet getINSTANCE() {
		if (INSTANCE == null)
			INSTANCE = new XtextResourceSet();
		return INSTANCE;
	}
}
