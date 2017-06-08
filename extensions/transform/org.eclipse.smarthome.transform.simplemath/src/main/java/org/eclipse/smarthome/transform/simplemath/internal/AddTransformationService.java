/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.simplemath.internal;

import org.eclipse.smarthome.core.transform.TransformationService;

import java.math.BigDecimal;

/**
 * The implementation of {@link TransformationService} which
 *
 * @author Martin van Wingerden
 */
public class AddTransformationService extends AbstractSimpleMathTransformationService {

    @Override
    BigDecimal performCalculation(BigDecimal source, BigDecimal value) {
        return source.add(value);
    }
}
