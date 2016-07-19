/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The {@link ThingDescriptionList} is the XML conversion result object which
 * is a list of {@link ThingTypeXmlResult}, {@link BridgeTypeXmlResult} and {@link ChannelTypeXmlResult} objects.
 *
 * @author Michael Grammling - Initial Contribution
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class ThingDescriptionList extends ArrayList {

    @SuppressWarnings("unchecked")
    public ThingDescriptionList(Collection list) {
        super(list);
    }

}
