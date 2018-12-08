/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.GenericThingHandler;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * MQTT topics are not inherently typed. Users are able to map topic values to framework types, for example
 * for numbers {@link NumberValue}, boolean values {@link OnOffValue}, number values
 * with limits {@link NumberValue} and string values {@link TextValue}.
 *
 * This interface allows the handler class {@link GenericThingHandler} to treat all MQTT topics the same.
 * {@link #getValue()} is used to retrieve the topic state and a call to {@link #update(Command)} sets the value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface Value {
    /**
     * Returns the item-type (one of {@link CoreItemFactory}).
     */
    String getItemType();

    /**
     * Return a list of supported command types. The order of the list is important.
     * <p>
     * The framework will try to parse an incoming string into one of those command types,
     * starting with the first and continue until it succeeds.
     * </p>
     */
    List<Class<? extends Command>> getSupportedCommandTypes();

    /**
     * Returns the current value state.
     */
    State getValue();

    /**
     * Updates the internal value state with the given command.
     *
     * @param command The command to update the internal value.
     * @return An updated value state. The same as if {@link #getValue()} is called.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    String update(Command command) throws IllegalArgumentException;

    /**
     * If the MQTT connection is not yet initialised or no values have
     * been received yet, the default value is {@link UnDefType#UNDEF}. To restore to the
     * default value after a connection got lost etc, this method will be called.
     */
    void resetState();

    StateDescription createStateDescription(String unit, boolean readOnly);
}
