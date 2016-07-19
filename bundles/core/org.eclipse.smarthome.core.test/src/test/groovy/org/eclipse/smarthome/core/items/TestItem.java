/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.List;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link GenericItem} implementation used for testing.
 *
 * @author Christoph Knauf - Initial contribution
 */
public class TestItem extends GenericItem {

    public TestItem(String name) {
        super("Test", name);
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return null;
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return null;
    }
}
