/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.core.persistence.dto.ItemHistoryDTO;

/**
 * This is a java bean that is used to serialize item lists.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ItemHistoryListDTO {
    public final List<ItemHistoryDTO> item = new ArrayList<ItemHistoryDTO>();

    public ItemHistoryListDTO() {
    }

    public ItemHistoryListDTO(Collection<ItemHistoryDTO> list) {
        item.addAll(list);
    }
}
