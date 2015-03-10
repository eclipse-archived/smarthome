/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation;

import java.util.Collection;

import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * This interface provides main functionality to manage {@link Rule}s in the
 * Rule Engine. It can add {@link Rule}s, get existing ones and remove them from
 * the Rule Engine.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface RuleRegistry extends Registry<Rule, String> {

    /**
     * This method is used to get collection of Rules which shares same tag.
     *
     * @param tag tag set to the rules
     * @return collection of {@link Rule}s having specified tag.
     */
    public Collection<Rule> getByTag(String tag);

    /**
     * This method is used for changing enable state of the Rule.
     *
     * @param uid unique identifier of the rule
     * @param isEnabled a new active state of the rule.
     */
    public void setEnabled(String uid, boolean isEnabled);

    /**
     * This method is used for checking active state of the Rule. Only inactive Rules can change their properties.
     *
     * @param uid unique identifier of the rule
     * @return true when the rule is active.
     */
    public boolean isEnabled(String uid);

    /**
     * This method is used for checking the running state of the Rule.
     * The Rule is running if it is triggered and execution of its actions are not finished.
     *
     * @param uid unique identifier of the rule
     * @return true when the Rule is running.
     */
    public boolean isRunning(String uid);

}
