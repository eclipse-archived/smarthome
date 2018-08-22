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
package org.eclipse.smarthome.io.iota.metadata;

import static java.util.stream.Collectors.toList;
import static org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder.create;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.core.metadata.MetadataConfigDescriptionProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Theo {@link IotaMetadataProvider} describes the metadata for the "iota" namespace and register the OSGi service.
 *
 * @author Theo Giovanna - initial contribution
 *
 */
@Component(service = MetadataConfigDescriptionProvider.class, configurationPid = "org.eclipse.smarthome.iota.metadata.IotaMetadataProvider", immediate = true)
@NonNullByDefault
public class IotaMetadataProvider implements MetadataConfigDescriptionProvider {

    @Override
    public String getNamespace() {
        return "iota";
    }

    @Override
    public @Nullable String getDescription(@Nullable Locale locale) {
        return "Share item state to the iota Tangle";
    }

    @Override
    public @Nullable List<ParameterOption> getParameterOptions(@Nullable Locale locale) {
        return Stream.of( //
                new ParameterOption("yes", "Share the item's state on the Tangle"), //
                new ParameterOption("no", "Don't share the item's state") //
        ).collect(toList());
    }

    @Override
    public @Nullable List<ConfigDescriptionParameter> getParameters(String value, @Nullable Locale locale) {
        switch (value) {
            case "yes":
                return Stream.of( //
                        create("mode", Type.TEXT).withLabel("Mode").withRequired(true).withLimitToOptions(true)
                                .withOptions( //
                                        Stream.of( //
                                                new ParameterOption("public", "Public"), //
                                                new ParameterOption("private", "Private"), //
                                                new ParameterOption("restricted", "Restricted") //
                                        ).collect(toList())).build(), //
                        create("key", Type.TEXT).withLabel("Private Key").withDescription(
                                "Leave blank for non-restricted mode, otherwise enter the private key you want to use")
                                .build(), //
                        create("seed", Type.TEXT).withLabel("Existing Seed Address").withDescription(
                                "Leave blank to publish on a new root. Insert an existing root address to publish on an existing stream")
                                .build() //
                ).collect(toList());
        }
        return null;
    }
}
