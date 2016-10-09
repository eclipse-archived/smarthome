/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2sql.internal;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.types.State;


/**
 * This is a Java bean used to return historic items from a SQL database.
 * 
 * @author Chris Jackson - Initial contribution
 *
 */
public class H2SqlHistoricItem implements HistoricItem {

	final private String name;
	final private State state;
	final private Date timestamp;
	
	public H2SqlHistoricItem(String name, State state, Date timestamp) {
		this.name = name;
		this.state = state;
		this.timestamp = timestamp;
	}
	
	public String getName() {
		return name;
	}
	
	public State getState() {
		return state;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance().format(timestamp) + ": " + name + " -> "+ state.toString();
	}

}