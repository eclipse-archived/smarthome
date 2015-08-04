/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;


/**
 * This class is responsible for custom serialization and deserialization of the {@link RuleTemplate}s. It is necessary
 * for the persistence of the {@link RuleTemplate}s. Implements {@link Externalizable}.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class PersistableRuleTemplate {

    /**
     * This constructor is used for deserialization of the {@link RuleTemplate}s.
     */
    public PersistableRuleTemplate() {
    }

}
