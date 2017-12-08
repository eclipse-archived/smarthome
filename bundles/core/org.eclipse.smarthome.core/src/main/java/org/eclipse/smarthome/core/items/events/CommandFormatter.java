/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.events;

import java.util.Map;

import org.eclipse.smarthome.core.types.Command;

/**
 * Formats and parses Command instances.
 *
 * @author Henning Treu - initial contribution
 *
 */
class CommandFormatter extends AbstractTypeFormatter<Command> {

    @Override
    String format(Command command) {
        return command.toFullString();
    }

    @Override
    Command parse(String type, String value, Map<String, String> stateMap) {
        return parseType(type, value, Command.class);
    }

}