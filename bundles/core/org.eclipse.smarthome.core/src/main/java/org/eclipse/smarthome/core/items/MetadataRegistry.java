/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * The MetadataRegistry is the central place, where additional information about items is kept.
 *
 * Metadata can be supplied by {@link MetadataProvider}s, which can provision them from any source
 * they like and also dynamically remove or add data.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public interface MetadataRegistry extends Registry<Metadata, MetadataKey> {

}
