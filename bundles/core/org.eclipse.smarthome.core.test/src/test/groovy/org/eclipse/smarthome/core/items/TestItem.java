/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.items;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
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
    public @NonNull List<@NonNull Class<? extends State>> getAcceptedDataTypes() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<@NonNull Class<? extends Command>> getAcceptedCommandTypes() {
        return Collections.emptyList();
    }
}
