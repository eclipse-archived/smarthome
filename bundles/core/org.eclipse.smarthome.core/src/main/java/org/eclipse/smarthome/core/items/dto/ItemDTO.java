/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.dto;

import java.util.List;
import java.util.Set;

/**
 * This is a data transfer object that is used to serialize items.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Andre Fuechsel - added tag support
 *
 */
public class ItemDTO {

    public String type;
    public String name;
    public String label;
    public String category;
    public Set<String> tags;
    public List<String> groupNames;

    public ItemDTO() {
    }

}
