/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared.simple;

import org.eclipse.smarthome.automation.Rule;

/**
 * convenience Rule class with a action handler. This allows to define Rules which have a execution block.
 *
 * @author Simon Merschjohann
 *
 */
public abstract class SimpleRule extends Rule implements SimpleRuleActionHandler {

}
