/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Simple class with the XStream mapping for a list of entries returned from a TclRega script.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@XStreamAlias("list")
public class TclScriptDataList {

    @XStreamImplicit(itemFieldName = "entry")
    private List<TclScriptDataEntry> entries = new ArrayList<TclScriptDataEntry>();

    /**
     * Returns all entries.
     */
    public List<TclScriptDataEntry> getEntries() {
        return entries;
    }

}
