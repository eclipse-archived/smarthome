/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.voice.Voice;
import org.eclipse.smarthome.core.voice.VoiceManager;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * Console command extension for all voice features.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class VoiceConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_SAY = "say";
    private static final String SUBCMD_INTERPRET = "interpret";
    private static final String SUBCMD_VOICES = "voices";

    private ItemRegistry itemRegistry;
    private VoiceManager voiceManager;

    public VoiceConsoleCommandExtension() {
        super("voice", "Commands around voice enablement features.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_SAY + " <text>", "speaks a text"),
                buildCommandUsage(SUBCMD_INTERPRET + " <command>", "interprets a human language command"),
                buildCommandUsage(SUBCMD_VOICES, "lists available voices of the active TTS service") });

    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_SAY:
                    if (args.length > 1) {
                        say((String[]) ArrayUtils.subarray(args, 1, args.length), console);
                    } else {
                        console.println("Specify text to say (e.g. 'say hello')");
                    }
                    return;
                case SUBCMD_INTERPRET:
                    if (args.length > 1) {
                        interpret((String[]) ArrayUtils.subarray(args, 1, args.length), console);
                    } else {
                        console.println("Specify text to interpret (e.g. 'interpret turn all lights off')");
                    }
                    return;
                case SUBCMD_VOICES:
                    for (Voice voice : voiceManager.getAllVoices()) {
                        console.println(
                                voice.getUID() + " " + voice.getLabel() + " - " + voice.getLocale().getDisplayName());
                    }
                    return;
                default:
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void interpret(String[] args, Console console) {
        StringBuilder sb = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(" ");
            sb.append(args[i]);
        }
        String msg = sb.toString();
        try {
            String result = voiceManager.interpret(msg);
            if (result != null) {
                console.println(result);
            }
        } catch (InterpretationException ie) {
            console.println(ie.getMessage());
        }
    }

    private void say(String[] args, Console console) {
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
        voiceManager.say(msg.toString());
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    protected void unsetVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = null;
    }

}
