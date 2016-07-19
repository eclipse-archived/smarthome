/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.autoupdate.internal;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The AutoUpdate-Binding is no 'normal' binding as it doesn't connect any hardware to the Eclipse SmartHome system. In
 * fact it takes care of updating the State of an item with respect to the received command automatically or not. By
 * default the State is getting updated automatically which is desired behavior in most of the cases. However it could
 * be useful to disable this default behavior.
 *
 * <p>
 * For example when implementing validation steps before changing a State one needs to control the State update oneself.
 *
 * @author Thomas.Eichstaedt-Engelen - Initial contribution
 * @author Kai Kreuzer - added sending real events
 * @author Stefan Bußweiler - Migration to new ESH event concept
 */
public class AutoUpdateBinding extends AbstractItemEventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(AutoUpdateBinding.class);

    protected ItemRegistry itemRegistry;

    /** to keep track of all binding config providers */
    protected Collection<AutoUpdateBindingConfigProvider> providers = new CopyOnWriteArraySet<>();

    protected EventPublisher eventPublisher = null;

    public void addBindingConfigProvider(AutoUpdateBindingConfigProvider provider) {
        providers.add(provider);
    }

    public void removeBindingConfigProvider(AutoUpdateBindingConfigProvider provider) {
        providers.remove(provider);
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * <p>
     * Iterates through all registered {@link AutoUpdateBindingConfigProvider}s and checks whether an autoupdate
     * configuration is available for <code>itemName</code>.
     * </p>
     *
     * <p>
     * If there are more then one {@link AutoUpdateBindingConfigProvider}s providing a configuration the results are
     * combined by a logical <em>OR</em>. If no configuration is provided at all the autoupdate defaults to
     * <code>true</code> and an update is posted for the corresponding {@link State}.
     * </p>
     *
     * @param itemName the item for which to find an autoupdate configuration
     * @param command the command being received and posted as {@link State} update if <code>command</code> is instance
     *            of {@link State} as well.
     */
    @Override
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        Boolean autoUpdate = null;
        String itemName = commandEvent.getItemName();
        Command command = commandEvent.getItemCommand();
        for (AutoUpdateBindingConfigProvider provider : providers) {
            Boolean au = provider.autoUpdate(itemName);
            if (au != null) {
                autoUpdate = au;
                if (Boolean.TRUE.equals(autoUpdate)) {
                    break;
                }
            }
        }

        // we didn't find any autoupdate configuration, so apply the default now
        if (autoUpdate == null) {
            autoUpdate = Boolean.TRUE;
        }

        if (autoUpdate && command instanceof State) {
            postUpdate(itemName, (State) command);
        } else {
            logger.trace("Won't update item '{}' as it is not configured to update its state automatically.", itemName);
        }
    }

    private void postUpdate(String itemName, State newState) {
        if (itemRegistry != null) {
            try {
                GenericItem item = (GenericItem) itemRegistry.getItem(itemName);
                boolean isAccepted = false;
                if (item.getAcceptedDataTypes().contains(newState.getClass())) {
                    isAccepted = true;
                } else {
                    // Look for class hierarchy
                    for (Class<? extends State> state : item.getAcceptedDataTypes()) {
                        try {
                            if (!state.isEnum()
                                    && state.newInstance().getClass().isAssignableFrom(newState.getClass())) {
                                isAccepted = true;
                                break;
                            }
                        } catch (InstantiationException e) {
                            logger.warn("InstantiationException on ", e.getMessage()); // Should never happen
                        } catch (IllegalAccessException e) {
                            logger.warn("IllegalAccessException on ", e.getMessage()); // Should never happen
                        }
                    }
                }
                if (isAccepted) {
                    eventPublisher.post(ItemEventFactory.createStateEvent(itemName, newState,
                            "org.eclipse.smarthome.core.autoupdate"));
                } else {
                    logger.debug("Received update of a not accepted type (" + newState.getClass().getSimpleName()
                            + ") for item " + itemName);
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Received update for non-existing item: {}", e.getMessage());
            }
        }
    }

}
