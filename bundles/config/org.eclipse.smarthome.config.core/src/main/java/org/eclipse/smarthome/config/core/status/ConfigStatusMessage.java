/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

/**
 * The {@link ConfigStatusMessage} is a domain object for a configuration status message. It contains the name
 * of the corresponding configuration parameter, a type information, the internationalized error message
 * and an optional status code.
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

    /** The corresponding internationalized status message. */
    public final String message;

    /**
     * The optional status code of the configuration status message; to be used if there are additional information to
     * be provided.
     */
    public final Integer statusCode;

    /**
     * Constructor.
     *
     * @param builder the configuration status message builder
     */
    private ConfigStatusMessage(Builder builder) {
        this.parameterName = builder.parameterName;
        this.type = builder.type;
        this.message = builder.message;
        this.statusCode = builder.statusCode;
    }

    /**
     * The builder for a {@link ConfigStatusMessage} object.
     */
    public static class Builder {

        private final String parameterName;

        private final Type type;

        private final String message;

        private Integer statusCode;

        private Builder(String parameterName, Type type, String message) {
            this.parameterName = parameterName;
            this.type = type;
            this.message = message;
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type
         * {@link Type#INFORMATION}.
         *
         * @param parameterName the name of the configuration parameter.
         * @param message the corresponding internationalized information message.
         *
         * @return the new builder instance
         */
        public static Builder information(String parameterName, String message) {
            return new Builder(parameterName, Type.INFORMATION, message);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#WARNING}.
         *
         * @param parameterName the name of the configuration parameter.
         * @param message the corresponding internationalized warning message.
         *
         * @return the new builder instance
         */
        public static Builder warning(String parameterName, String message) {
            return new Builder(parameterName, Type.WARNING, message);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#ERROR}.
         *
         * @param parameterName the name of the configuration parameter.
         * @param message the corresponding internationalized error message.
         *
         * @return the new builder instance
         */
        public static Builder error(String parameterName, String message) {
            return new Builder(parameterName, Type.ERROR, message);
        }

        /**
         * Creates a builder for the construction of a {@link ConfigStatusMessage} having type {@link Type#PENDING}.
         *
         * @param parameterName the name of the configuration parameter.
         * @param message the corresponding internationalized error message.
         *
         * @return the new builder instance
         */
        public static Builder pending(String parameterName, String message) {
            return new Builder(parameterName, Type.PENDING, message);
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
        result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigStatusMessage other = (ConfigStatusMessage) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (parameterName == null) {
            if (other.parameterName != null)
                return false;
        } else if (!parameterName.equals(other.parameterName))
            return false;
        if (statusCode == null) {
            if (other.statusCode != null)
                return false;
        } else if (!statusCode.equals(other.statusCode))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConfigStatusMessage [parameterName=" + parameterName + ", type=" + type + ", message=" + message
                + ", statusCode=" + statusCode + "]";
    }
}
