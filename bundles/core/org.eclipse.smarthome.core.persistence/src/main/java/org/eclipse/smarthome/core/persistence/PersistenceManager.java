/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

public interface PersistenceManager {
    void addConfig(String dbId, PersistenceServiceConfiguration config);

    void removeConfig(String dbId);

    void startEventHandling(String dbId);

    void stopEventHandling(String modelName);
}
