/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public class GroupItem extends GenericItem implements StateChangeListener {

    private final Logger logger = LoggerFactory.getLogger(GroupItem.class);

    protected final GenericItem baseItem;

    protected final Set<Item> members;

    protected GroupFunction function;

    public GroupItem(String name) {
        this(name, null);
    }

    public GroupItem(String name, GenericItem baseItem) {
        this(name, baseItem, new GroupFunction.Equality());
    }

    public GroupItem(String name, GenericItem baseItem, GroupFunction function) {
        super("Group", name);
        members = new CopyOnWriteArraySet<Item>();
        this.function = function;
        this.baseItem = baseItem;
    }

    /**
     * Returns the base item of this {@link GroupItem}. This method is only
     * intended to allow instance checks of the underlying BaseItem. It must
     * not be changed in any way.
     * 
     * @return the base item of this GroupItem
     */
    public GenericItem getBaseItem() {
        return baseItem;
    }

    /**
     * Returns the direct members of this {@link GroupItem} regardless if these
     * members are {@link GroupItem}s as well.
     * 
     * @return the direct members of this {@link GroupItem}
     */
    public Set<Item> getMembers() {
        return ImmutableSet.copyOf(members);
    }

    /**
     * Returns the direct members of this {@link GroupItem} and recursively all
     * members of the potentially contained {@link GroupItem}s as well. The {@link GroupItem}s itself aren't contained.
     * The returned items are unique.
     * 
     * @return all members of this and all contained {@link GroupItem}s
     */
    public Set<Item> getAllMembers() {
        Set<Item> allMembers = new HashSet<Item>();
        collectMembers(allMembers, members);
        return ImmutableSet.copyOf(allMembers);
    }

    private void collectMembers(Set<Item> allMembers, Set<Item> members) {
        for (Item member : members) {
            if (member instanceof GroupItem) {
                collectMembers(allMembers, ((GroupItem) member).members);
            } else {
                allMembers.add(member);
            }
        }
    }

    public void addMember(Item item) {
        members.add(item);
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.addStateChangeListener(this);
        }
    }

    public void removeMember(Item item) {
        members.remove(item);
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.removeStateChangeListener(this);
        }
    }

    /**
     * The accepted data types of a group item is the same as of the underlying base item.
     * If none is defined, the intersection of all sets of accepted data types of all group
     * members is used instead.
     * 
     * @return the accepted data types of this group item
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends State>> getAcceptedDataTypes() {
        if (baseItem != null) {
            return baseItem.getAcceptedDataTypes();
        } else {
            List<Class<? extends State>> acceptedDataTypes = null;

            for (Item item : members) {
                if (acceptedDataTypes == null) {
                    acceptedDataTypes = new ArrayList<>(item.getAcceptedDataTypes());
                } else {
                    acceptedDataTypes.retainAll(item.getAcceptedDataTypes());
                }
            }
            return acceptedDataTypes == null ? Collections.unmodifiableList(Collections.EMPTY_LIST) : Collections
                    .unmodifiableList(acceptedDataTypes);
        }
    }

    /**
     * The accepted command types of a group item is the same as of the underlying base item.
     * If none is defined, the intersection of all sets of accepted command types of all group
     * members is used instead.
     * 
     * @return the accepted command types of this group item
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        if (baseItem != null) {
            return baseItem.getAcceptedCommandTypes();
        } else {
            List<Class<? extends Command>> acceptedCommandTypes = null;

            for (Item item : members) {
                if (acceptedCommandTypes == null) {
                    acceptedCommandTypes = new ArrayList<>(item.getAcceptedCommandTypes());
                } else {
                    acceptedCommandTypes.retainAll(item.getAcceptedCommandTypes());
                }
            }
            return acceptedCommandTypes == null ? Collections.unmodifiableList(Collections.EMPTY_LIST) : Collections
                    .unmodifiableList(acceptedCommandTypes);
        }
    }

    public void send(Command command) {
        if (getAcceptedCommandTypes().contains(command.getClass())) {
            internalSend(command);
        } else {
            logger.warn("Command '{}' has been ignored for group '{}' as it is not accepted.", command.toString(),
                    getName());
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void internalSend(Command command) {
        if (eventPublisher != null) {
            for (Item member : members) {
                // try to send the command to the bus
                eventPublisher.post(ItemEventFactory.createCommandEvent(member.getName(), command));
            }
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public State getStateAs(Class<? extends State> typeClass) {
        State newState = function.getStateAs(getAllMembers(), typeClass);
        if (newState == null && baseItem != null) {
            // we use the transformation method from the base item
            baseItem.setState(state);
            newState = baseItem.getStateAs(typeClass);
        }
        if (newState == null) {
            newState = super.getStateAs(typeClass);
        }
        return newState;
    }

    /**
     * @{inheritDoc
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" (");
        sb.append("Type=");
        sb.append(getClass().getSimpleName());
        sb.append(", ");
        if (getBaseItem() != null) {
            sb.append("BaseType=");
            sb.append(baseItem.getClass().getSimpleName());
            sb.append(", ");
        }
        sb.append("Members=");
        sb.append(members.size());
        sb.append(", ");
        sb.append("State=");
        sb.append(getState());
        sb.append(", ");
        sb.append("Label=");
        sb.append(getLabel());
        sb.append(", ");
        sb.append("Category=");
        sb.append(getCategory());
        if (!getTags().isEmpty()) {
            sb.append(", ");
            sb.append("Tags=[");
            sb.append(Joiner.on(", ").join(getTags()));
            sb.append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        setState(function.calculate(members));
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void stateUpdated(Item item, State state) {
        setState(function.calculate(members));
    }

}
