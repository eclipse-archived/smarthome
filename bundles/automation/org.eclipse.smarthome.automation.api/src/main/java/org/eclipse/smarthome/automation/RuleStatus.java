/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.List;

/**
 * This interface is used to present status of rule. The status has following properties:
 * The rule can be enable/disable - this property can be set by the user when the rule
 * must stop work for temporary period of time. The rule can me running when it is
 * executing triggered data. The rule can be not initialized when some of module handlers
 * are not available.
 * 
 * @author Yordan Mihaylov
 */
public interface RuleStatus {

    boolean isEnabled();

    boolean isRunning();

    boolean isInitialize();

    public List<RuleError> getErrors();
}
