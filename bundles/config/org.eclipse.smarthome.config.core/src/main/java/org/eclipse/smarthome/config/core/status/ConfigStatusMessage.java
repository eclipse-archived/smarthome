/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * The {@link ConfigStatusMessage} is a domain object for a configuration status message. It contains the name
 * of the configuration parameter, the {@link ConfigStatusMessage.Type} information, the key for the message to be
 * internationalized, arguments to be added to the message and an optional status code.
 * <p>
 * The framework will take care of setting {@link ConfigStatusMessage#message} with the corresponding internationalized
 * message. For this purpose there should be an i18n properties file inside the bundle of the
 * {@link ConfigStatusProvider} that has a message declared for the {@link ConfigStatusMessage#messageKey}.
 * </p>
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigStatusMessage {

    /**
     * The {@link Type} defines an enumeration of all supported types for a configuration status message.
     */
    public enum Type {

        /**
         * The type for an information message. It is used to provide some general information about a configuration
         * parameter.
         */
        INFORMATION,

        /**
         * The type for a warning message. It should be used if there might be some issue with the configuration
         * parameter.
         */
        WARNING,

        /**
         * The type for an error message. It should be used if there is a severe issue with the configuration parameter.
         */
        ERROR,

        /**
         * The type for a pending message. It should be used if the transmission of the configuration parameter to the
         * entity is pending.
         */
        PENDING;
    }

    /** The name of the configuration parameter. */
    public final String parameterName;

    /** The {@link Type} of the configuration status message. */
    public final Type type;

    /** The key for the message to be internalized. */
    final transient String messageKey;

    /** The arguments to be injected into the internationalized message. */
    final transient Object[] arguments;

    /** The corresponding internationalized status message. */
    public final String message;

    /**
     * The optional status code of the configuration status message; to be used if there are additional information to
     * be provided.
     */
    public final Integer statusCode;

    /**
     * Creates a new {@link ConfigStatusMessage}.
     *
     * @param builder the configuration status message builder
     */
    private ConfigStatusMessage(Builder builder) {
        this.parameterName = builder.parameterName;
        this.type = builder.type;
        this.messageKey = builder.messageKey;
        this.arguments = builder.arguments;
        this.message = null;
        this.statusCode = builder.statusCode;
    }

    /**
     * Creates a new {@link ConfigStatusMessage} with an internationalized message.
     *
     * @param parameterName the name of the configuration parameter
     * @param type the {@link Type} of the configuration status message
     * @param message the corresponding internationalized status message
     * @param statusCode the optional status code
     */
    ConfigStatusMessage(String parameterName, Type type, String message, Integer statusCode) {
        this(parameterName, type, null, null, message, statusCode);
    }

    private ConfigStatusMessage(String parameterName, Type type, String messageKey, Object[] arguments, String message,
            Integer statusCode) {
        this.parameterName = parameterName;
        this.type = type;
        this.messageKey = messageKey;
        this.arguments = arguments;
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * The builder for a {@link ConfigStatusMessage} object.
     */
    public static class Builder {

        private final String parameterName;

        private final Type type;

        private final String messageKey;

        private Object[] arguments;

        private Integer statusCode;

        private Builder(String parameterName, Type type, String messageKey) {
            Preconditions.checkNotNull(parameterName, "Parameter name must not be null.");
            Preconditions.checkNotNull(type, "Type must not be null.");
            Preconditions.checkNotNull(messageKey, "Message key must not be null.");
            this.parameterName = parameterName;
            this.type = type;
            this.messageKey = messageKey;
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type
         * {@link Type#INFORMATION}.
         *
         * @param parameterName the name of the configuration parameter (must not be null)
         * @param messageKey the key for the message to be internationalized (must not be null)
         *
         * @return the new builder instance
         */
        public static Builder information(String parameterName, String messageKey) {
            return new Builder(parameterName, Type.INFORMATION, messageKey);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#WARNING}.
         *
         * @param parameterName the name of the configuration parameter (must not be null)
         * @param messageKey the key for the message to be internationalized (must not be null)
         *
         * @return the new builder instance
         */
        public static Builder warning(String parameterName, String messageKey) {
            return new Builder(parameterName, Type.WARNING, messageKey);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#ERROR}.
         *
         * @param parameterName the name of the configuration parameter (must not be null)
         * @param messageKey the key for the message to be internationalized (must not be null)
         *
         * @return the new builder instance
         */
        public static Builder error(String parameterName, String messageKey) {
            return new Builder(parameterName, Type.ERROR, messageKey);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#PENDING}.
         *
         * @param parameterName the name of the configuration parameter (must not be null)
         * @param messageKey the key for the message to be internationalized (must not be null)
         *
         * @return the new builder instance
         */
        public static Builder pending(String parameterName, String messageKey) {
            return new Builder(parameterName, Type.PENDING, messageKey);
        }

        /**
         * Adds the given arguments (to be injected into the internationalized message) to the builder.
         *
         * @param arguments the arguments to be added
         *
         * @return the updated builder instance
         */
        public Builder withArguments(Object... arguments) {
            this.arguments = arguments;
            return this;
        }

        /**
         * Adds the given status code to the builder.
         *
         * @param statusCode the status code to be added
         *
         * @return the updated builder
         */
        public Builder withStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Builds the new {@link ConfigStatusMessage} object.
         *
         * @return new {@link ConfigStatusMessage} object.
         */
        public ConfigStatusMessage build() {
            return new ConfigStatusMessage(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(arguments);
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((messageKey == null) ? 0 : messageKey.hashCode());
        result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
        result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfigStatusMessage other = (ConfigStatusMessage) obj;
        if (!Arrays.equals(arguments, other.arguments)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (messageKey == null) {
            if (other.messageKey != null) {
                return false;
            }
        } else if (!messageKey.equals(other.messageKey)) {
            return false;
        }
        if (parameterName == null) {
            if (other.parameterName != null) {
                return false;
            }
        } else if (!parameterName.equals(other.parameterName)) {
            return false;
        }
        if (statusCode == null) {
            if (other.statusCode != null) {
                return false;
            }
        } else if (!statusCode.equals(other.statusCode)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConfigStatusMessage [parameterName=" + parameterName + ", type=" + type + ", messageKey=" + messageKey
                + ", arguments=" + Arrays.toString(arguments) + ", message=" + message + ", statusCode=" + statusCode
                + "]";
    }

}
