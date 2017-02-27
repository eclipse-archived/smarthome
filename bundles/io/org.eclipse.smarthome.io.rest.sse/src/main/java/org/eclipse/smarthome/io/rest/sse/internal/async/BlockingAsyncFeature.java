/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.async;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * A {@link Feature} implementation that registers our custom {@link BlockingAsyncBinder}.
 *
 * @author Ivan Iliev - Initial Contribution and API
 *
 */
public class BlockingAsyncFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        if (context.getConfiguration().isEnabled(BlockingAsyncFeature.class)) {
            return false;
        }

        context.register(new BlockingAsyncBinder());

        return true;
    }
}
