package org.eclipse.smarthome.core.items.events;

import java.util.Map;

import org.eclipse.smarthome.core.types.Command;

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