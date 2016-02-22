/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.multimedia.tts.TTSService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Console command extension to speak text by a text-to-speech service (TTS)
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class SayConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private ItemRegistry itemRegistry;

    public SayConsoleCommandExtension() {
        super("say", "Speak text by a text-to-speech service (TTS).");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<text>", "speaks a text"));
    }

    @Override
    public void execute(String[] args, Console console) {
        StringBuilder msg = new StringBuilder();
        for (String word : args) {
            if (word.startsWith("%") && word.endsWith("%") && word.length() > 2) {
                String itemName = word.substring(1, word.length() - 1);
                try {
                    Item item = this.itemRegistry.getItemByPattern(itemName);
                    msg.append(item.getState().toString());
                } catch (ItemNotFoundException e) {
                    console.println("Error: Item '" + itemName + "' does not exist.");
                } catch (ItemNotUniqueException e) {
                    console.print("Error: Multiple items match this pattern: ");
                    for (Item item : e.getMatchingItems()) {
                        console.print(item.getName() + " ");
                    }
                }
            } else {
                msg.append(word);
            }
            msg.append(" ");
        }

        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        TTSService ttsService = getTTSService(context, System.getProperty("osgi.os"));
        if (ttsService == null) {
            ttsService = getTTSService(context, "any");
        }
        if (ttsService != null) {
            ttsService.say(msg.toString(), null, null);
            console.println("Said: " + msg);
        } else {
            console.println("No TTS service available - tried to say: " + msg);
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * Queries the OSGi service registry for a service that provides a TTS implementation
     * for a given platform.
     *
     * @param context the bundle context to access the OSGi service registry
     * @param os a valid osgi.os string value or "any" if service should be platform-independent
     * @return a service instance or null, if none could be found
     */
    static private TTSService getTTSService(BundleContext context, String os) {
        if (context != null) {
            String filter = os != null ? "(os=" + os + ")" : null;
            try {
                Collection<ServiceReference<TTSService>> refs = context.getServiceReferences(TTSService.class, filter);
                if (refs != null && refs.size() > 0) {
                    return context.getService(refs.iterator().next());
                } else {
                    return null;
                }
            } catch (InvalidSyntaxException e) {
                // this should never happen
            }
        }
        return null;
    }

}
