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
package org.eclipse.smarthome.automation.core.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class allows the easy construction of a {@link Module} instance using the builder pattern.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Split implementation for different module types in sub classes
 *
 */
@NonNullByDefault
public abstract class ModuleBuilder<B extends ModuleBuilder<B, T>, T extends Module> {

    private @Nullable String id;
    private @Nullable String typeUID;
    protected @Nullable Configuration configuration;
    protected @Nullable String label;
    protected @Nullable String description;

    protected ModuleBuilder() {
    }

    protected ModuleBuilder(T module) {
        this.id = module.getId();
        this.typeUID = module.getTypeUID();
        this.configuration = module.getConfiguration();
        this.label = module.getLabel();
        this.description = module.getDescription();
    }

    public B withId(String id) {
        this.id = id;
        return (B) this;
    }

    public B withTypeUID(String typeUID) {
        this.typeUID = typeUID;
        return (B) this;
    }

    public B withConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return (B) this;
    }

    public B withLabel(@Nullable String label) {
        this.label = label;
        return (B) this;
    }

    public B withDescription(@Nullable String description) {
        this.description = description;
        return (B) this;
    }

    protected String getId() {
        final String id = this.id;
        if (id == null) {
            throw new IllegalStateException("The ID must not be null.");
        }
        return id;
    }

    protected String getTypeUID() {
        final String typeUID = this.typeUID;
        if (typeUID == null) {
            throw new IllegalStateException("The type UID must not be null.");
        }
        return typeUID;
    }

    public abstract T build();
}
