/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal.multiverse;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.smarthome.binding.dmx.DmxBindingConstants.ListenerType;
import org.eclipse.smarthome.binding.dmx.internal.DmxThingHandler;
import org.eclipse.smarthome.binding.dmx.internal.Util;
import org.eclipse.smarthome.binding.dmx.internal.action.BaseAction;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Channel} extends {@link BaseChannel} with actions and values
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Davy Vanherbergen
 */
public class Channel extends BaseChannel {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 255;

    private final Logger logger = LoggerFactory.getLogger(Channel.class);

    private int value = MIN_VALUE;
    private int suspendedValue = MIN_VALUE;
    private int lastStateValue = -1;

    private boolean isSuspended;
    private long lastStateTimestamp = 0;

    private List<BaseAction> actions = new ArrayList<BaseAction>();
    private List<BaseAction> suspendedActions = new ArrayList<BaseAction>();
    private List<Thing> registeredThings = new ArrayList<Thing>();

    private HashMap<ChannelUID, DmxThingHandler> onOffListeners = new HashMap<ChannelUID, DmxThingHandler>();
    private HashMap<ChannelUID, DmxThingHandler> valueListeners = new HashMap<ChannelUID, DmxThingHandler>();
    private Entry<ChannelUID, DmxThingHandler> actionListener = null;

    public Channel(int universeId, int channelId) {
        super(universeId, channelId);
    }

    public Channel(BaseChannel channel) {
        super(channel);
    }

    /**
     * register a thing with this channel
     *
     * @param thing a Thing object
     */
    public void registerThing(Thing thing) {
        if (!registeredThings.contains(thing)) {
            logger.debug("registering {} from Channel {}", thing, this);
            registeredThings.add(thing);
        }
    }

    /**
     * unregister a thing from this object
     *
     * @param thing a Thing object
     */
    public void unregisterThing(Thing thing) {
        if (registeredThings.contains(thing)) {
            logger.debug("removing {} from Channel {}", thing, this);
            registeredThings.remove(thing);
        }
    }

    /**
     * check if Channel has any registered objects
     *
     * @return true or false
     */
    public boolean hasRegisteredThings() {
        return !registeredThings.isEmpty();
    }

    /**
     * set a channel value
     *
     * @param value Integer value (0-255)
     */
    public void setValue(int value) {
        this.value = Util.toDmxValue(value) << 8;
        logger.trace("set channel {} to value {}", this, this.value >> 8);
    }

    /**
     * set a channel value
     *
     * @param value PercentType (0-100)
     */
    public void setValue(PercentType value) {
        this.value = Util.toDmxValue(value) << 8;
        logger.trace("set channel {} to value {}", this, this.value >> 8);
    }

    /**
     * get the value of this channel
     *
     * @return value as Integer (0-255)
     */
    public int getValue() {
        return Util.toDmxValue(value >> 8);
    }

    /**
     * get the value of this channel
     *
     * @return value as Integer (0-65535)
     */
    public int getHiResValue() {
        return value;
    }

    /**
     * suspends current value and actions
     */
    public synchronized void suspendAction() {
        suspendedValue = value;
        isSuspended = true;
        suspendedActions.clear();
        suspendedActions.addAll(actions);
        if (isSuspended) {
            logger.info("second suspend for actions in channel {}, previous will be lost", this);
        } else {
            logger.trace("suspending actions for channel {}", this);
        }
    }

    /**
     * resumes previously suspended actions
     */
    public synchronized void resumeAction() throws IllegalStateException {
        if (isSuspended) {
            clearAction();
            if (!suspendedActions.isEmpty()) {
                actions.addAll(suspendedActions);
                suspendedActions.clear();
            } else {
                value = suspendedValue;
            }
            isSuspended = false;
            logger.trace("resuming suspended actions for channel {}", this);
        } else {
            throw new IllegalStateException("trying to resume actions in non-suspended channel " + this.toString());
        }
    }

    /**
     * check suspended state
     *
     * @return true or false
     */
    public boolean isSuspended() {
        return isSuspended;
    }

    /**
     * clear all running actions
     */
    public synchronized void clearAction() {
        logger.trace("clearing all actions for channel {}", this);
        actions.clear();
        // remove action listener
        if (actionListener != null) {
            actionListener.getValue().updateState(actionListener.getKey(), OnOffType.OFF);
            logger.trace("sending ACTION status update to listener {}", actionListener.getKey());
            actionListener = null;
        }
    }

    /**
     * Replace the current list of channel actions with the provided one.
     *
     * @param channelAction action for this channel.
     */
    public synchronized void setChannelAction(BaseAction channelAction) {
        clearAction();
        actions.add(channelAction);
        logger.trace("set action {} for channel {}", channelAction, this);
    }

    /**
     * Add a channel action to the current list of channel actions.
     *
     * @param channelAction action for this channel.
     */
    public synchronized void addChannelAction(BaseAction channelAction) {
        actions.add(channelAction);
        logger.trace("added action {} to channel {} (total {} actions)", channelAction, this, actions.size());
    }

    /**
     * @return true if there are running actions
     */
    public boolean hasRunningActions() {
        return !actions.isEmpty();
    }

    /**
     * Move to the next action in the action chain. This method is used by
     * automatic chains and to manually move to the next action if actions are
     * set as indefinite (e.g. endless hold). This allows the user to toggle
     * through a fixed set of values.
     */
    public synchronized void switchToNextAction() {
        // push action to the back of the action list
        BaseAction action = actions.get(0);
        actions.remove(0);
        action.reset();
        actions.add(action);
        logger.trace("switching to next action {} on channel {}", actions.get(0), this);
    }

    /**
     * Get the new value for this channel as determined by active actions or the
     * current value.
     *
     * @param calculationTime UNIX timestamp
     * @return value 0-255
     */
    public synchronized Integer getNewValue(long calculationTime) {
        return (getNewHiResValue(calculationTime) >> 8);
    }

    /**
     * Get the new value for this channel as determined by active actions or the
     * current value.
     *
     * @param calculationTime UNIX timestamp
     * @return value 0-65535
     */
    public synchronized Integer getNewHiResValue(long calculationTime) {
        if (hasRunningActions()) {
            BaseAction action = actions.get(0);
            value = action.getNewValue(this, calculationTime);
            if (action.isCompleted()) {
                switchToNextAction();
            }
        }

        // send updates not more than once in a second, and only on value change
        if ((lastStateValue != value) && (calculationTime - lastStateTimestamp > 1000)) {
            // notify value listeners if value changed
            for (Entry<ChannelUID, DmxThingHandler> listener : valueListeners.entrySet()) {
                PercentType state = Util.toPercentValue(Util.toDmxValue(value >> 8));
                (listener.getValue()).updateState(listener.getKey(), state);
                logger.trace("sending VALUE status update to listener {}", listener.getKey());
            }

            // notify on/off listeners if on/off state changed
            if ((lastStateValue == 0) || (value == 0)) {
                OnOffType state = (value == 0) ? OnOffType.OFF : OnOffType.ON;
                for (Entry<ChannelUID, DmxThingHandler> listener : onOffListeners.entrySet()) {
                    (listener.getValue()).updateState(listener.getKey(), state);
                    logger.trace("sending ONOFF status update to listener {}", listener.getKey());
                }
            }

            lastStateValue = value;
            lastStateTimestamp = calculationTime;
        }

        return value;
    }

    /**
     * add a channel listener for state updates
     *
     * @param thingChannel the channel the listener is linked to
     * @param listener the listener itself
     */
    public void addListener(ChannelUID thingChannel, DmxThingHandler listener, ListenerType type) {
        switch (type) {
            case ONOFF:
                if (onOffListeners.containsKey(thingChannel)) {
                    logger.trace("ONOFF listener {} already exists in channel {}", thingChannel, this);
                } else {
                    onOffListeners.put(thingChannel, listener);
                    logger.debug("adding ONOFF listener {} to channel {}", thingChannel, this);
                }
                break;
            case VALUE:
                if (valueListeners.containsKey(thingChannel)) {
                    logger.trace("VALUE listener {} already exists in channel {}", thingChannel, this);
                } else {
                    valueListeners.put(thingChannel, listener);
                    logger.debug("adding VALUE listener {} to channel {}", thingChannel, this);
                }
                break;
            case ACTION:
                if (actionListener != null) {
                    logger.info("replacing action listener {} with {} in channel {}", actionListener.getValue(),
                            listener, this);
                } else {
                    logger.debug("adding action listener {} in channel {}", listener, this);
                }
                actionListener = new AbstractMap.SimpleEntry<ChannelUID, DmxThingHandler>(thingChannel, listener);
            default:
        }
    }

    /**
     * remove listener from channel
     *
     * @param thingChannel the channel that shall no longer receive updates
     */
    public void removeListener(ChannelUID thingChannel) {
        boolean foundListener = false;
        if (onOffListeners.containsKey(thingChannel)) {
            onOffListeners.remove(thingChannel);
            foundListener = true;
            logger.debug("removing ONOFF listener {} from channel {}", thingChannel, this);
        }
        if (valueListeners.containsKey(thingChannel)) {
            valueListeners.remove(thingChannel);
            foundListener = true;
            logger.debug("removing VALUE listener {} from channel {}", thingChannel, this);
        }
        if (actionListener != null && actionListener.getKey().equals(thingChannel)) {
            actionListener = null;
            foundListener = true;
            logger.debug("removing ACTION listener {} from channel {}", thingChannel, this);
        }
        if (!foundListener) {
            logger.trace("listener {} not found in channel {}", thingChannel, this);
        }
    }

}
