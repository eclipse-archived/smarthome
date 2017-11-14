/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.types.State;

/**
 * This is a java bean that is used to serialize items to JSON.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ItemHistoryDTO {

    public String name;
    public String totalrecords;
    public String datapoints;

    public List<HistoryDataBean> data = new ArrayList<HistoryDataBean>();

    public ItemHistoryDTO() {
    };

    /**
     * Add a new record to the data history.
     * This method returns a double value equal to the state. This may be used for comparison by the caller.
     *
     * @param time the time of the record
     * @param state the state at this time
     */
    public void addData(Long time, State state) {
        HistoryDataBean newVal = new HistoryDataBean();
        newVal.time = time;
        newVal.state = state.toString();
        data.add(newVal);
    }

    public static class HistoryDataBean {
        public Long time;
        public String state;
    }
}
