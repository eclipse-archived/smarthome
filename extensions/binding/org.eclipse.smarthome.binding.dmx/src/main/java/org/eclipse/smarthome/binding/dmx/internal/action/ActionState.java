/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal.action;

/**
 * The {@link ActionState} gives the state of an action
 *
 * waiting : not started yet
 * running : action is running
 * completed : action has completed, proceed to next action
 * completedfinal : action has completed, hold here
 *
 * @author Jan N. Klug
 */
public enum ActionState {
    waiting,
    running,
    completed,
    completedfinal
}
