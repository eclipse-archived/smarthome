/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
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
