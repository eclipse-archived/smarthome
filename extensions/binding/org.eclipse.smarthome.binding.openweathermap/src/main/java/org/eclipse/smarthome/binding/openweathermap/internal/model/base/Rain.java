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
package org.eclipse.smarthome.binding.openweathermap.internal.model.base;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Generated Plain Old Java Objects class for {@link Rain} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Rain {
    @SerializedName("3h")
    private @Nullable Double threeHours;

    public @Nullable Double get3h() {
        return threeHours;
    }

    public void set3h(Double threeHours) {
        this.threeHours = threeHours;
    }
}
