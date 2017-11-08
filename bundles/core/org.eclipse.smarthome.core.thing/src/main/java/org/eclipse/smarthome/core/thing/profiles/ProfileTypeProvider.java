/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * A {@link ProfileTypeProvider} is responsible for providing {@link ProfileType}s.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ProfileTypeProvider extends Provider<ProfileType> {

}
