/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.beans;

/**
 * Event bean for broadcasted events.
 *
 * @author Ivan Iliev - Initial Contribution and API
 * @author Dennis Nobel - Added event type and renamed object to payload
 */
public class EventBean {

    public String topic;

    public String payload;
    
    public String type;

}
