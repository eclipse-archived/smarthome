/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.List;

/**
 * This is a java bean that is used to serialize channel group definitions.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ChannelGroupDefinitionBean {

    public String id;
    public String description;
    public String label;
    public List<ChannelDefinitionBean> channels;

    public ChannelGroupDefinitionBean() {
    }

    public ChannelGroupDefinitionBean(String id, String label, String description, List<ChannelDefinitionBean> channels) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.channels = channels;
    }

}
