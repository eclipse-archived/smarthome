/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

/**
 * The {@link ScanListener} interface for receiving scan operation events.
 * <p>
 * A class that is interested in errors and termination of an active scan has to implement this interface.
 *
 * @author Kai Kreuzer - Initial Contribution.
 *
 * @see DiscoveryService
 */
public interface ScanListener {

    /**
     * Invoked synchronously when the according scan has finished.
     * <p>
     * This signal is sent latest when the defined timeout for the scan has been reached.
     */
    void onFinished();

    /**
     * Invoked synchronously when the according scan has caused an error or has been aborted.
     *
     * @param exception the error which occurred (could be null)
     */
    void onErrorOccurred(Exception exception);

}
