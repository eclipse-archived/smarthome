/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

/**
 * An handle to a {@link STTService}
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface STTServiceHandle {
   /**
    * Aborts recognition in the associated {@link STTService}
    */
    public void abort();
}
