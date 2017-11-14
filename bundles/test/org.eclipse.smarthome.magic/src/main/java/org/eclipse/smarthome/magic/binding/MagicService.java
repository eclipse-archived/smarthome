/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.magic.binding;

import java.net.URI;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;

/**
 * A public interface for a service from this virtual bundle which is also a {@link ConfigOptionProvider}.
 *
 * @author Henning Treu - Initial contribution
 *
 */
public interface MagicService extends ConfigOptionProvider {

    static final URI CONFIG_URI = URI.create("test:magic");

}
