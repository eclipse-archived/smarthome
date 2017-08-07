/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.magic.binding.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.magic.binding.MagicService;

import com.google.common.collect.Lists;

/**
 *
 * @author Henning Treu - Initial contribution
 */
public class MagicServiceImpl implements MagicService {

    static final String PARAMETER_BACKEND_DECIMAL = "select_decimal_limit";

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (!uri.equals(CONFIG_URI)) {
            return null;
        }

        if (param.equals(PARAMETER_BACKEND_DECIMAL)) {
            return Lists.newArrayList(new ParameterOption(BigDecimal.ONE.toPlainString(), "1"),
                    new ParameterOption(BigDecimal.TEN.toPlainString(), "10"),
                    new ParameterOption(BigDecimal.valueOf(21d).toPlainString(), "21"));
        }

        return null;
    }

}
