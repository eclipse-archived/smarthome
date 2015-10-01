/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.test;

import static org.junit.Assert.*

import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.xml.internal.ChannelTypeXmlResult
import org.eclipse.smarthome.core.thing.xml.internal.ThingDescriptionList
import org.eclipse.smarthome.core.thing.xml.internal.ThingDescriptionReader
import org.junit.Test


/**
 * The {@link Example} test case is a usage example how the according {@code ThingType} parser
 * can be used. This example can also be used for manual tests when the schema is extended or
 * changed.
 *
 * @author Michael Grammling - Initial Contribution.
 */
class Example {

    @Test
    public void test() {
        File file = new File("./example/example.xml")
        URL channelsURL = file.toURI().toURL()

        ThingDescriptionReader reader = new ThingDescriptionReader()
        ThingDescriptionList thingList = reader.readFromXML(channelsURL)

        thingList.each {
            print it

            if (it instanceof ChannelTypeXmlResult) {
                ChannelType channelType = ((ChannelTypeXmlResult) it).toChannelType()

                print ", advanced=" + channelType.isAdvanced() +
                        ", itemType=" + channelType.getItemType() +
                        ", label=" + channelType.getLabel() +
                        ", description=" + channelType.getDescription() +
                        ", category=" + channelType.getCategory() +
                        ", tags=" + channelType.getTags() +
                        ", state=" + channelType.getState();
            }

            print "\n"
        }
    }
}
