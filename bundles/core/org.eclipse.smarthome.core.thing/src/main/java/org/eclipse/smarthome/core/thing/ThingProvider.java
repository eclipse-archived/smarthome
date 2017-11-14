/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * The {@link ThingProvider} is responsible for providing things.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Dennis Nobel - Changed interface to extend {@link Provider} interface
 */
public interface ThingProvider extends Provider<Thing> {

}
