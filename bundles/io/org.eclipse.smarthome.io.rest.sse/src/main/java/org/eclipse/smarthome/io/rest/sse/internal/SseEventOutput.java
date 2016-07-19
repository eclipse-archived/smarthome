/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal;

import java.io.IOException;
import java.util.List;

import org.eclipse.smarthome.io.rest.sse.beans.EventBean;
import org.eclipse.smarthome.io.rest.sse.internal.util.SseUtil;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

/**
 * {@link EventOutput} implementation that takes a filter parameter and only writes out events that match this filter.
 * Should only be used when the {@link OutboundEvent}s sent through this {@link EventOutput} contain a data object of
 * type {@link EventBean}
 * 
 * @author Ivan Iliev - Initial contribution and API
 * 
 */
public class SseEventOutput extends EventOutput {

    private List<String> regexFilters;

    public SseEventOutput(String topicFilter) {
        super();
        this.regexFilters = SseUtil.convertToRegex(topicFilter);
    }

    @Override
    public void write(OutboundEvent chunk) throws IOException {
        EventBean event = (EventBean) chunk.getData();

        for (String filter : regexFilters) {
            if (event.topic.matches(filter)) {
                super.write(chunk);
                return;
            }
        }
    }

}
