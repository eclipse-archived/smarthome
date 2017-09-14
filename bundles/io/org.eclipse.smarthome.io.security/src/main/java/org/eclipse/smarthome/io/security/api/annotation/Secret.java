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
package org.eclipse.smarthome.io.security.api.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.smarthome.io.security.api.SecretsVaultService;

/**
 * This is a helper annotation, useful for the {@link config.core.Configuration} class.
 *
 * If you annotate a field in your services configuration holder object as "Secret", then
 * its value will not be touched/read/written by the OSGI configuration admin service. As a consequence
 * it will not be stored non-encrypted on disk and will not be available to other bundles and services.
 *
 * A usage example in the form of a configuration holder object looks like this:
 *
 * ```
 * class YourConfig {
 * public String username;
 * public @Secret("pwd") Bytebuffer password;
 * public @Secret Bytebuffer bitcoinWalletKey;
 * };
 * ```
 *
 * What happens instead is this:
 *
 * # On load
 * When loading a service, the configuration dispatcher service will retrieve the services
 * configuration and the services `activate(@Nullable Map<String, Object> configMap)` method
 * is called. The `Secret` annotated fields will be null/not-set in that map.
 * If you follow the configuration holder object pattern, you will do something like:
 * `YourConfig config = new Configuration(configMap).as(YourConfig.class);`.
 * Your `Secret` annotated fields will automatically be retrieved from {@link SecretsVaultService} and are
 * instantly available for consumption.
 *
 * # On store:
 * When a configuration is pushed to the configuration admin service, the values of all your
 * annotated fields are stored in the {@link SecretsVaultService} and nulled. The
 * configuration admin service will never see any secrets.
 *
 * Usually you do not push a complete configuration object to the config admin service though. You can
 * store individual fields as usual with the {@link SecretsVaultService} API.
 *
 * @author David Graeff - Inital contribution
 *
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Secret {
    public String secretKey() default "";
}
