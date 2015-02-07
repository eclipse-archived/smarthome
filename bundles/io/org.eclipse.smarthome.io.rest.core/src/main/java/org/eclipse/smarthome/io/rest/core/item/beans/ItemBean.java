/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.item.beans;

import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This is a java bean that is used with JAXB to serialize items to JSON.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Andre Fuechsel - added tag support
 *
 */
public class ItemBean {

    public String type;
    public String name;
    public String label;
    public String category;
    public String state;
    public String link;
    public Set<String> tags;
    public StateDescription stateDescription;
    public List<String> groupNames;

    public ItemBean() {
    }

}
