/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.eclipse.smarthome.core.types.UnDefType;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The abstract base class for all items. It provides all relevant logic
 * for the infrastructure, such as publishing updates to the event bus
 * or notifying listeners.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Andre Fuechsel - Added tags
 * @author Stefan Bußweiler - Migration to new ESH event concept
 *
 */
abstract public class GenericItem implements ActiveItem {

    protected EventPublisher eventPublisher;

    protected Set<StateChangeListener> listeners = new CopyOnWriteArraySet<StateChangeListener>(
            Collections.newSetFromMap(new WeakHashMap<StateChangeListener, Boolean>()));

    protected List<String> groupNames = new ArrayList<String>();

    protected Set<String> tags = new HashSet<String>();

    final protected String name;

    final protected String type;

    protected State state = UnDefType.NULL;

    protected String label;

    protected String category;

    private List<StateDescriptionProvider> stateDescriptionProviders;

    public GenericItem(String type, String name) {
        this.name = name;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getStateAs(Class<? extends State> typeClass) {
        if (typeClass != null && typeClass.isInstance(state)) {
            return state;
        } else {
            return null;
        }
    }

    public void initialize() {
    }

    public void dispose() {
        this.eventPublisher = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getGroupNames() {
        return ImmutableList.copyOf(groupNames);
    }

    /**
     * Adds a group name to the {@link GenericItem}.
     *
     * @param groupItemName
     *            group item name to add
     */
    @Override
    public void addGroupName(String groupItemName) {
        if (!groupNames.contains(groupItemName)) {
            groupNames.add(groupItemName);
        }
    }

    @Override
    public void addGroupNames(String... groupItemNames) {
        for (String groupItemName : groupItemNames) {
            addGroupName(groupItemName);
        }
    }

    @Override
    public void addGroupNames(List<String> groupItemNames) {
        for (String groupItemName : groupItemNames) {
            addGroupName(groupItemName);
        }
    }

    /**
     * Removes a group item name from the {@link GenericItem}.
     *
     * @param groupItemName
     *            group item name to remove
     */
    @Override
    public void removeGroupName(String groupItemName) {
        groupNames.remove(groupItemName);
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setStateDescriptionProviders(List<StateDescriptionProvider> stateDescriptionProviders) {
        this.stateDescriptionProviders = stateDescriptionProviders;
    }

    protected void internalSend(Command command) {
        // try to send the command to the bus
        if (eventPublisher != null) {
            eventPublisher.post(ItemEventFactory.createCommandEvent(this.getName(), command));
        }
    }

    /**
     * Sets new state and notifies listeners.
     *
     * @param state
     *            new state of this item
     */
    public void setState(State state) {
        State oldState = this.state;
        this.state = state;
        notifyListeners(oldState, state);
    }

    public void send(RefreshType command) {
        internalSend(command);
    }

    private void notifyListeners(State oldState, State newState) {
        // if nothing has changed, we send update notifications
        Set<StateChangeListener> clonedListeners = null;
        clonedListeners = new CopyOnWriteArraySet<StateChangeListener>(listeners);
        for (StateChangeListener listener : clonedListeners) {
            listener.stateUpdated(this, newState);
        }
        if (newState!=null && !newState.equals(oldState)) {
            for (StateChangeListener listener : clonedListeners) {
                listener.stateChanged(this, oldState, newState);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" (");
        sb.append("Type=");
        sb.append(getClass().getSimpleName());
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

    public void addStateChangeListener(StateChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeStateChangeListener(StateChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }


    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericItem other = (GenericItem) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
    public Set<String> getTags() {
        return ImmutableSet.copyOf(tags);
    }

    @Override
    public boolean hasTag(String tag) {
        return (tags.contains(tag));
    }

    @Override
    public void addTag(String tag) {
        tags.add(tag);
    }

    @Override
    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
    }

    @Override
    public void addTags(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    @Override
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    @Override
    public void removeAllTags() {
        tags.clear();
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public StateDescription getStateDescription() {
        return getStateDescription(Locale.getDefault());
    }

    @Override
    public StateDescription getStateDescription(Locale locale) {
        if(stateDescriptionProviders != null) {
            for (StateDescriptionProvider stateDescriptionProvider : stateDescriptionProviders) {
                StateDescription stateDescription = stateDescriptionProvider.getStateDescription(this.name, locale);
                if(stateDescription != null) {
                    return stateDescription;
                }
            }
        }
        return null;
    }

}
