/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * A {@link RuleProvider} is responsible for providing rules.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface RuleProvider extends Provider<Rule> {

}
