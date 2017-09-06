/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

/**
 * Common ancestor of all profile types.
 *
 * Profiles define the communication flow between the framework and bindings, i.e. how (and if) certain events and
 * commands are forwarded from the framework to the thing handler and vice versa.
 * <p>
 * Profiles are allowed to maintain some transient state internally, i.e. the same instance of a profile will be used
 * per link for all communication so that the temporal dimension can be taken in account.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface Profile {

}
