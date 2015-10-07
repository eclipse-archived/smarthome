/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxNetworkThrottler} is helper class that regulates the frequency at which messages/packets are sent to
 * LIFX bulbs. the LIFX LAN Protocol Specification states that bulbs can process up to 20 messages per second, not more
 *
 * @author Karel Goderis - Initial Contribution
 */
public class LifxNetworkThrottler {

    private static Logger logger = LoggerFactory.getLogger(LifxNetworkThrottler.class);

    public final static long PACKET_INTERVAL = 50;

    private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();
    private static ConcurrentHashMap<String, Long> timestamps = new ConcurrentHashMap<String, Long>();

    public static void lock(String key) {
        if (!locks.containsKey(key)) {
            locks.put(key, new ReentrantLock());
        }

        locks.get(key).lock();

        if (timestamps.get(key) != null) {

            long lastStamp = timestamps.get(key);
            long timeToWait = Math.max(PACKET_INTERVAL - (System.currentTimeMillis() - lastStamp), 0);
            if (timeToWait > 0) {
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred while putting the thread to sleep : '{}'", e.getMessage());
                }
            }
        }

    }

    public static void unlock(String key) {

        if (locks.containsKey(key)) {
            timestamps.put(key, System.currentTimeMillis());
            locks.get(key).unlock();
        }
    }

    public static void lock() {

        for (ReentrantLock aLock : locks.values()) {
            aLock.lock();
        }

        long lastStamp = 0;

        for (Long aStamp : timestamps.values()) {
            if (aStamp > lastStamp) {
                lastStamp = aStamp;
            }
        }

        long timeToWait = Math.max(PACKET_INTERVAL - (System.currentTimeMillis() - lastStamp), 0);
        if (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                logger.error("An exception occurred while putting the thread to sleep : '{}'", e.getMessage());
            }
        }
    }

    public static void unlock() {

        for (String key : locks.keySet()) {
            timestamps.put(key, System.currentTimeMillis());
            locks.get(key).unlock();
        }

    }

}
