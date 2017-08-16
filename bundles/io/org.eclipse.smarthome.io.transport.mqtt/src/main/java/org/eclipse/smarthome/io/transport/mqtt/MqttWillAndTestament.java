/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

import org.apache.commons.lang.StringUtils;

/**
 * Class encapsulating the last will and testament that is published after the client has gone offline.
 *
 * @author Markus Mann
 *
 */
public class MqttWillAndTestament {
    private String topic;
    private byte[] payload;
    private int qos = 0;
    private boolean retain = false;

    /**
     * Create an instance of the last will using a string with the following format:<br/>
     * topic:message:qos:retained <br/>
     * Where
     * <ul>
     * <li>topic is a normal topic string (no placeholders are allowed)</li>
     * <li>message the message to send</li>
     * <li>qos Valid values are 0 (Deliver at most once),1 (Deliver at least once) or 2</li>
     * <li>retain true if messages shall be retained</li>
     * </ul>
     *
     * @param string the string to parse. If null, null is returned
     * @return the will instance, will be null only if parameter is null
     */
    public static MqttWillAndTestament fromString(String string) {
        if (string == null) {
            return null;
        }
        MqttWillAndTestament result = new MqttWillAndTestament();
        String[] components = string.split(":");
        for (int i = 0; i < Math.min(components.length, 4); i++) {
            String value = StringUtils.trimToEmpty(components[i]);
            switch (i) {
                case 0:
                    result.topic = value;
                    break;
                case 1:
                    result.payload = value.getBytes();
                    break;
                case 2:
                    if (!"".equals(value)) {
                        int qos = Integer.valueOf(value);
                        if (qos >= 0 && qos <= 2) {
                            result.qos = qos;
                        }
                    }
                    break;
                case 3:
                    result.retain = Boolean.valueOf(value);
                    break;
            }
        }
        return result.isValid() ? result : null;
    }

    /**
     * Hide the constructor and force consumers to use the fromString() method or the
     * constructor requiring all field parameters to be set.
     */
    private MqttWillAndTestament() {
    }

    /**
     * Create a new {@link} MqttWillAndTestament with at least a topic name.
     *
     * @param topic topic is a normal topic string (no placeholders are allowed)
     * @param payload The optional payload. Can be null.
     * @param qos Valid values are 0 (Deliver at most once),1 (Deliver at least once) or 2</li>
     * @param retain true if messages shall be retained
     */
    public MqttWillAndTestament(String topic, byte[] payload, int qos, boolean retain) {
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("Topic must be set");
        }
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retain = retain;
    }

    /**
     * Return true if the last will and testament object is valid.
     */
    private boolean isValid() {
        return !StringUtils.isBlank(topic);
    }

    /**
     * @return the topic for the last will.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the payload of the last will.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @return quality of service level.
     */
    public int getQos() {
        return qos;
    }

    /**
     * @return true if the last will should be retained by the broker.
     */
    public boolean isRetain() {
        return retain;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getClass());
        sb.append("] Send '");
        if (payload != null) {
            sb.append(new String(payload));
        } else {
            sb.append(payload);
        }
        sb.append("' to topic '");
        sb.append(topic);
        sb.append("'");
        if (retain) {
            sb.append(" retained");
        }
        sb.append(" using qos mode ").append(qos);
        return sb.toString();
    }

}
