/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.io.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.io.voice.text.InterpretationException;

/**
 * A human language command interpretation service.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class StandardHumanLanguageInterpreter implements HumanLanguageInterpreter {

    private ItemRegistry itemRegistry;

    private EventPublisher eventPublisher;

    private final Set<Locale> supportedLocales = Collections.unmodifiableSet(Collections.singleton(Locale.ENGLISH));

    private final String PARDON = "I didn't quite follow that.";

    private final String MORE_THAN_ONE = "There's more than one thing with a similar name.";

    private final String NO_ONE = "There is no thing named like that.";

    private final String STATE_ON = "on";

    private final String STATE_OFF = "off";

    @Override
    public String interpret(Locale locale, String text) throws InterpretationException {
        if (locale != Locale.ENGLISH) {
            throw new InterpretationException(
                    locale.getDisplayLanguage(Locale.ENGLISH) + " is not supported at the moment.");
        }
        String[] tokens = text.trim().toLowerCase().split("\\s++");
        if (tokens.length == 0) {
            throw new InterpretationException(PARDON);
        }

        String first = tokens[0];
        if (first.equals("turn") && tokens.length >= 3) {

            // parse
            int pos = 1, end = tokens.length;
            String state = STATE_ON;
            if (tokens[1].equals(STATE_ON)) {
                pos++;
            } else if (tokens[1].equals(STATE_OFF)) {
                pos++;
                state = STATE_OFF;
            } else if (tokens[end - 1].equals(STATE_ON)) {
                end--;
            } else if (tokens[end - 1].equals(STATE_OFF)) {
                end--;
                state = STATE_OFF;
            } else {
                throw new InterpretationException(PARDON);
            }

            // search
            ArrayList<Item> items = getMatchingItems(Arrays.copyOfRange(tokens, pos, end), OnOffType.class);

            // execute
            if (items.size() <= 0) {
                throw new InterpretationException(NO_ONE);
            } else if (items.size() > 1) {
                throw new InterpretationException(MORE_THAN_ONE);
            } else {
                Item item = items.get(0);
                OnOffType oldState = (OnOffType) item.getStateAs(OnOffType.class);
                OnOffType newState = OnOffType.valueOf(state.toUpperCase());
                if (oldState.equals(newState)) {
                    return "It's already " + state + ".";
                }
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), newState));
                return "Ok";
            }

        } else {
            throw new InterpretationException(PARDON);
        }
    }

    /**
     * Filters the item registry by matching each item's name with the provided name fragments.
     * For this the item's name is at first tokenized by {@link splitName}.
     * The resulting fragments are now looked up by each and every provided fragment.
     * For the item to get included into the result list, every provided fragment has to be found among the item's ones.
     * If a command type is provided, the item also has to support it.
     *
     * @param nameFragments name fragments that are used to match an item's name.
     *            For a positive match, the item's name has to contain every fragment - independently of their order.
     *            They are treated case insensitive.
     * @param commandType optional command type that all items have to support.
     *            Provide {null} if there is no need for a certain command to be supported.
     * @return All matching items from the item registry.
     */
    private ArrayList<Item> getMatchingItems(String[] nameFragments, Class<?> commandType) {
        ArrayList<Item> items = new ArrayList<Item>();
        for (Item item : itemRegistry.getAll()) {
            HashSet<String> parts = new HashSet<String>(splitName(item.getName(), true));
            boolean allMatch = true;
            for (String fragment : nameFragments) {
                allMatch = allMatch && parts.contains(fragment.toLowerCase());
            }
            if (allMatch && (commandType == null || item.getAcceptedCommandTypes().contains(commandType))) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Splits an item's name into single words. It splits whitespace, Pascal, Camel and Snake-casing.
     *
     * @param name the name that's to be split
     * @param toLowerCase if {true}, all resulting fragments will be made lower case
     * @return resulting fragments of the name
     */
    private ArrayList<String> splitName(String name, boolean toLowerCase) {
        String[] split = name.split("(?<!^)(?=[A-Z])|_|\\s+");
        ArrayList<String> parts = new ArrayList<String>();
        for (int i = 0; i < split.length; i++) {
            String part = split[i].trim();
            if (part.length() > 0) {
                if (toLowerCase) {
                    part = part.toLowerCase();
                }
                parts.add(part);
            }
        }
        return parts;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

}
