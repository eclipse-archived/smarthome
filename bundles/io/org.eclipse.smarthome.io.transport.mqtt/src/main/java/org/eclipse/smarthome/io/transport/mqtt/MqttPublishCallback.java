/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * Implement this to be notified of the success or error of a {@link MqttBrokerConnection}.publish().
 *
 * @author David Graeff - Initial contribution
 */
public interface MqttPublishCallback {
    public void onSuccess(MqttPublishResult result);

    public void onFailure(MqttPublishResult result, Throwable error);
}
