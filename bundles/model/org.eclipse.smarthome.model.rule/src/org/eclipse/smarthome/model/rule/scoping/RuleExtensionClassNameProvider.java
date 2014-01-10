/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.scoping;

import org.eclipse.smarthome.model.script.scoping.ScriptExtensionClassNameProvider;

import com.google.inject.Singleton;

/**
 * This class registers all statically available functions as well as the
 * extensions for specific jvm types, which should only be available in rules,
 * but not in scripts
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Singleton
public class RuleExtensionClassNameProvider extends	ScriptExtensionClassNameProvider {

}
