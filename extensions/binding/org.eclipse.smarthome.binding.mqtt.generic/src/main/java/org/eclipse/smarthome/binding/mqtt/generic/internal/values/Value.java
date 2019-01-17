/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * MQTT topics are not inherently typed.
 *
 * <p>
 * With this class users are able to map MQTT topic values to framework types,
 * for example for numbers {@link NumberValue}, boolean values {@link OnOffValue}, percentage values
 * {@link PercentageValue}, string values {@link TextValue} and more.
 * </p>
 *
 * <p>
 * This class and the encapsulated (cached) state are necessary, because MQTT can't be queried,
 * but we still need to be able to respond to framework requests for a value.
 * </p>
 *
 * <p>
 * {@link #getCache()} is used to retrieve a topic state and a call to {@link #update(Command)} sets the value.
 * </p>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class Value {
    protected State state = UnDefType.UNDEF;
    protected final List<Class<? extends Command>> commandTypes;
    private final String itemType;

    protected Value(String itemType, List<Class<? extends Command>> commandTypes) {
        this.itemType = itemType;
        this.commandTypes = commandTypes;
    }

    /**
     * Return a list of supported command types. The order of the list is important.
     * <p>
     * The framework will try to parse an incoming string into one of those command types,
     * starting with the first and continue until it succeeds.
     * </p>
     * <p>
     * Your {@link #update(Command)} method must accept all command types of this list.
     * You may accept more command types. This allows you to restrict the parsing of incoming
     * MQTT values to the listed types, but handle more user commands.
     * </p>
     * A prominent example is the {@link NumberValue}, which does not return {@link PercentType},
     * because that would interfere with {@link DecimalType} while parsing the MQTT value.
     * It does however accept a {@link PercentType} for {@link #update(Command)}, because a
     * linked Item could send that type of command.
     */
    public final List<Class<? extends Command>> getSupportedCommandTypes() {
        return commandTypes;
    }

    /**
     * Returns the item-type (one of {@link CoreItemFactory}).
     */
    public final String getItemType() {
        return itemType;
    }

    /**
     * Returns the current value state.
     */
    public final State getChannelState() {
        return state;
    }

    public String getMQTTpublishValue() {
        return state.toString();
    }

    /**
     * Returns true if this is a binary type.
     */
    public boolean isBinary() {
        return false;
    }

    /**
     * If the MQTT connection is not yet initialised or no values have
     * been received yet, the default value is {@link UnDefType#UNDEF}. To restore to the
     * default value after a connection got lost etc, this method will be called.
     */
    public final void resetState() {
        state = UnDefType.UNDEF;
    }

    /**
     * Updates the internal value state with the given command.
     *
     * @param command The command to update the internal value.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public abstract void update(Command command) throws IllegalArgumentException;

    /**
     * Returns the given command if it cannot be handled by {@link #update(Command)}
     * or {@link #update(byte[])} and need to be posted straight to the framework instead.
     * Returns null otherwise.
     *
     * @param command The command to decide about
     */
    public @Nullable Command isPostOnly(Command command) {
        return null;
    }

    /**
     * Updates the internal value state with the given binary payload.
     *
     * @param data The binary payload to update the internal value.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public void update(byte data[]) throws IllegalArgumentException {
        String mimeType = null;

        // URLConnection.guessContentTypeFromStream(input) is not sufficient to detect all JPEG files
        if (data.length >= 2 && data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[data.length - 2] == (byte) 0xFF
                && data[data.length - 1] == (byte) 0xD9) {
            mimeType = "image/jpeg";
        } else {
            try (final ByteArrayInputStream input = new ByteArrayInputStream(data)) {
                try {
                    mimeType = URLConnection.guessContentTypeFromStream(input);
                } catch (final IOException ignored) {
                }
            } catch (final IOException ignored) {
            }
        }
        state = new RawType(data, mimeType == null ? RawType.DEFAULT_MIME_TYPE : mimeType);
    }

    /**
     * Return the state description for this value state.
     *
     * @param unit An optional unit string. Might be an empty string.
     * @param readOnly True if this is a read-only value.
     * @return A state description
     */
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), readOnly,
                Collections.emptyList());
    }
}
