/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ${package}.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link ${bindingIdCamelCase}Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author ${author} - Initial contribution
 */
public class ${bindingIdCamelCase}Handler extends BaseThingHandler {

	public ${bindingIdCamelCase}Handler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if(channelUID.getId().equals("channel1")) {
            // TODO: handle command
        }
	}
}
