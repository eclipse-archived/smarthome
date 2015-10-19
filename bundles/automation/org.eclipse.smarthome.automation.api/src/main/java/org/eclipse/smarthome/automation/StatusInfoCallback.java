/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

/**
 * This interface is used by {@link RuleRegistry} implementation to be notified of changes related to statuses of rules.
 *
 * @author Yordan Mihaylov - initial contribution
 */
public interface StatusInfoCallback {

    /**
     * The method is called when the rule has update of its status.
     * 
     * @param ruleUID UID of the {@link Rule}
     * @param statusInfo new status info releated to the {@link Rule}
     */
    void statusInfoChanged(String ruleUID, RuleStatusInfo statusInfo);
}
