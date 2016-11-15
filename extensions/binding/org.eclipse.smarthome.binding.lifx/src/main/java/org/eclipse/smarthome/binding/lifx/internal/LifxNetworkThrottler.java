/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.PACKET_INTERVAL;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxNetworkThrottler} is a helper class that regulates the frequency at which messages/packets are sent to
 * LIFX bulbs. The LIFX LAN Protocol Specification states that bulbs can process up to 20 messages per second, not more.
 *
 * @author Karel Goderis - Initial Contribution
 * @author Wouter Born - Deadlock fix
 */
public class LifxNetworkThrottler {

    private static Logger logger = LoggerFactory.getLogger(LifxNetworkThrottler.class);

    /**
     * Tracks when the last packet was sent to a LIFX bulb. The packet is sent after obtaining the lock and before
     * releasing the lock.
     */
    private static class LifxLightCommunicationTracker {

        private long timestamp;

        private ReentrantLock lock = new ReentrantLock();

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            // When iterating over all trackers another thread may have inserted this object so this thread may not
            // have a lock on it. When the thread does not have the lock, it also did not send a packet.
            if (lock.isHeldByCurrentThread()) {
                timestamp = System.currentTimeMillis();
                lock.unlock();
            }
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * A separate list of trackers is maintained when locking all lights in case of a broadcast. Iterators of
     * {@link ConcurrentHashMap}s may behave non-linear when inserts take place to obtain more concurrency. When the
     * iterator of {@code values()} of {@link #macTrackerMapping} is used for locking all lights, it could sometimes
     * cause deadlock.
     */
    private static List<LifxLightCommunicationTracker> trackers = new CopyOnWriteArrayList<>();

    private static Map<String, LifxLightCommunicationTracker> macTrackerMapping = new ConcurrentHashMap<String, LifxLightCommunicationTracker>();

    public static void lock(String mac) {
        LifxLightCommunicationTracker tracker = getOrCreateTracker(mac);
        tracker.lock();
        waitForNextPacketInterval(tracker.getTimestamp());
    }

    private static LifxLightCommunicationTracker getOrCreateTracker(String mac) {
        LifxLightCommunicationTracker tracker = macTrackerMapping.get(mac);
        if (tracker == null) {
            // for better performance only synchronize when necessary
            synchronized (trackers) {
                // another thread may just have added a tracker in this synchronized block, so reevaluate
                tracker = macTrackerMapping.get(mac);
                if (tracker == null) {
                    tracker = new LifxLightCommunicationTracker();
                    trackers.add(tracker);
                    macTrackerMapping.put(mac, tracker);
                }
            }
        }
        return tracker;
    }

    private static void waitForNextPacketInterval(long timestamp) {
        long timeToWait = Math.max(PACKET_INTERVAL - (System.currentTimeMillis() - timestamp), 0);
        if (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                logger.error("An exception occurred while putting the thread to sleep : '{}'", e.getMessage());
            }
        }
    }

    public static void unlock(String mac) {
        if (macTrackerMapping.containsKey(mac)) {
            macTrackerMapping.get(mac).unlock();
        }
    }

    public static void lock() {
        long lastStamp = 0;
        for (LifxLightCommunicationTracker tracker : trackers) {
            tracker.lock();
            lastStamp = Math.max(lastStamp, tracker.getTimestamp());
        }
        waitForNextPacketInterval(lastStamp);
    }

    public static void unlock() {
        for (LifxLightCommunicationTracker tracker : trackers) {
            tracker.unlock();
        }
    }

}
