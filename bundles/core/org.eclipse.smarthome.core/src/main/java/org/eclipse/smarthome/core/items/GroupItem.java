/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class GroupItem extends GenericItem implements StateChangeListener {

    @NonNull
    public static final String TYPE = "Group";

    private final Logger logger = LoggerFactory.getLogger(GroupItem.class);

    protected final GenericItem baseItem;

    protected final Set<Item> members;

    protected GroupFunction function;

    /**
     * Creates a plain GroupItem
     *
     * @param name name of the group
     */
    public GroupItem(@NonNull String name) {
        this(name, null, null);
    }

    public GroupItem(@NonNull String name, GenericItem baseItem) {
        // only baseItem but no function set -> use Equality
        this(name, baseItem, new GroupFunction.Equality());
    }

    /**
     * Creates a GroupItem with function
     *
     * @param name name of the group
     * @param baseItem type of items in the group
     * @param function function to calculate group status out of member status
     */
    public GroupItem(@NonNull String name, GenericItem baseItem, GroupFunction function) {
        super(TYPE, name);

        // we only allow GroupItem with BOTH, baseItem AND function set, or NONE of them set
        if (baseItem == null || function == null) {
            this.baseItem = null;
            this.function = null;
        } else {
            this.function = function;
            this.baseItem = baseItem;
        }

        members = new CopyOnWriteArraySet<Item>();
    }

    /**
     * Returns the base item of this {@link GroupItem}. This method is only
     * intended to allow instance checks of the underlying BaseItem. It must
     * not be changed in any way.
     *
     * @return the base item of this GroupItem
     */
    public Item getBaseItem() {
        return baseItem;
    }

    /**
     * Returns the function of this {@link GroupItem}.
     *
     * @return the function of this GroupItem
     */
    public GroupFunction getFunction() {
        return function;
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
        return ImmutableSet.copyOf(getMembers((Item i) -> !(i instanceof GroupItem)));
    }

    private void collectMembers(Set<Item> allMembers, Set<Item> members) {
        for (Item member : members) {
            if (allMembers.contains(member)) {
                continue;
            }
            allMembers.add(member);
            if (member instanceof GroupItem) {
                collectMembers(allMembers, ((GroupItem) member).members);
            }
        }
    }

    /**
     * Retrieves ALL members of this group and filters it with the given Predicate
     *
     * @param filterItem Predicate with settings to filter member list
     * @return Set of member items filtered by filterItem
     */
    public Set<Item> getMembers(Predicate<Item> filterItem) {
        Set<Item> allMembers = new HashSet<Item>();
        collectMembers(allMembers, members);
        return allMembers.stream().filter(filterItem).collect(Collectors.toSet());
    }

    /**
     * Adds the given item to the members of this group item.
     *
     * @param item the item to be added (must not be null)
     * @throws IllegalArgumentException if the given item is null
     */
    public void addMember(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item must not be null!");
        }
        members.add(item);
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.addStateChangeListener(this);
        }
    }

    /**
     * Removes the given item from the members of this group item.
     *
     * @param item the item to be removed (must not be null)
     * @throws IllegalArgumentException if the given item is null
     */
    public void removeMember(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item must not be null!");
        }
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
            return acceptedDataTypes == null ? Collections.unmodifiableList(Collections.EMPTY_LIST)
                    : Collections.unmodifiableList(acceptedDataTypes);
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
            return acceptedCommandTypes == null ? Collections.unmodifiableList(Collections.EMPTY_LIST)
                    : Collections.unmodifiableList(acceptedCommandTypes);
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

    @Override
    protected void internalSend(Command command) {
        if (eventPublisher != null) {
            for (Item member : members) {
                // try to send the command to the bus
                eventPublisher.post(ItemEventFactory.createCommandEvent(member.getName(), command));
            }
        }
    }

    @Override
    public State getStateAs(Class<? extends State> typeClass) {
        // if a group does not have a function it cannot have a state
        State newState = null;
        if (function != null) {
            newState = function.getStateAs(getAllMembers(), typeClass);
        }

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
        if (!getGroupNames().isEmpty()) {
            sb.append(", ");
            sb.append("Groups=[");
            sb.append(Joiner.on(", ").join(getGroupNames()));
            sb.append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
    }

    @Override
    public void stateUpdated(Item item, State state) {
        State oldState = this.state;
        if (function != null && baseItem != null) {
            State calculatedState = function.calculate(members);
            calculatedState = ItemUtil.convertToAcceptedState(calculatedState, baseItem);
            setState(calculatedState);
        }
        if (!oldState.equals(this.state)) {
            sendGroupStateChangedEvent(item.getName(), this.state, oldState);
        }
    }

    @Override
    public void setState(State state) {
        State oldState = this.state;
        if (baseItem != null) {
            baseItem.setState(state);
            this.state = baseItem.getState();
        } else {
            this.state = state;
        }
        notifyListeners(oldState, state);
    }

    private void sendGroupStateChangedEvent(String memberName, State newState, State oldState) {
        if (eventPublisher != null) {
            eventPublisher.post(
                    ItemEventFactory.createGroupStateChangedEvent(this.getName(), memberName, newState, oldState));
        }
    }

}
