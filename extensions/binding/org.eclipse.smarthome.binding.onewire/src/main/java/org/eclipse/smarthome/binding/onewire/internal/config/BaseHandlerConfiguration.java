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
package org.eclipse.smarthome.binding.onewire.internal.config;

import java.math.BigDecimal;

/**
 * The {@link BaseHandlerConfiguration} is a helper class for the base thing handler configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BaseHandlerConfiguration {
    public String id;
    public BigDecimal refresh;
}
