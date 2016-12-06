/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal.action;

import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;

/**
 * Resume action. Restores previously suspended value or actions on an item.
 *
 * @author Davy Vanherbergen
 * @author Jan N. Klug
 */
public class ResumeAction extends BaseAction {

    @Override
    public int getNewValue(Channel channel, long currentTime) {
        channel.resumeAction();
        return channel.getNewHiResValue(currentTime);
    }

}
