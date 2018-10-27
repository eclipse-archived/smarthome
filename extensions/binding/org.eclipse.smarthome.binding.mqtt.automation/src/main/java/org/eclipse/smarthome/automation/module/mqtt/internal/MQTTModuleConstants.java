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
package org.eclipse.smarthome.automation.module.mqtt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault()
public class MQTTModuleConstants {
    public static final String INOUT_TOPIC_NAME = "topicName";
    public static final String INOUT_TOPIC_VALUE = "topicValue";
    public static final String INOUT_BROKER_CONNECTION = "brokerId";

    public static final String TYPE_TOPIC = "topic";
    public static final String TYPE_STRING = "string";

    public static final String CFG_BROKER = "mqttbroker";
    public static final String CFG_TOPIC = "topic";
    public static final String CFG_MESSAGE = "message";
    public static final String CFG_TIMEOUT = "timeout";
    public static final String CFG_RETAINED = "retained";

}
