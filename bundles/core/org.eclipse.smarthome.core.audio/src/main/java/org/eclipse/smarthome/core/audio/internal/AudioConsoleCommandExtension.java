/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * Console command extension for all audio features.
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - refactored to match AudioManager implementation
 *
 */
public class AudioConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_GROUPS = "group";
    private static final String SUBCMD_PLAY = "play";
    private static final String SUBCMD_STREAM = "stream";
    private static final String SUBCMD_SOURCES = "sources";
    private static final String SUBCMD_SINKS = "sinks";

    private AudioManager audioManager;

    public AudioConsoleCommandExtension() {
        super("audio", "Commands around audio enablement features.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] {
                buildCommandUsage(SUBCMD_PLAY + " <sink or group> <filename>",
                        "plays a sound file from the sounds folder through the optionally specified audio sink(s)"),
                buildCommandUsage(SUBCMD_STREAM + " <sink or group> <url>",
                        "streams the sound from the url through the optionally specified audio sink(s)"),
                buildCommandUsage(SUBCMD_SOURCES, "lists the audio sources"),
                buildCommandUsage(SUBCMD_SINKS, "lists the audio sinks"),
                buildCommandUsage(SUBCMD_GROUPS + " create <name>", "create a group of sinks"),
                buildCommandUsage(SUBCMD_GROUPS + " clear <name>", "remove a group of sinks"),
                buildCommandUsage(SUBCMD_GROUPS + " add <group> <sink>", "add a sink to a group of sinks"),
                buildCommandUsage(SUBCMD_GROUPS + " remove <group> <sink>", "remove a sink to a group of sinks") });

    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_PLAY:
                    if (args.length > 1) {
                        play((String[]) ArrayUtils.subarray(args, 1, args.length), console);
                    } else {
                        console.println(
                                "Specify file to play, and optionally the sink(s) to use (e.g. 'play javasound hello.mp3')");
                    }
                    return;
                case SUBCMD_STREAM:
                    if (args.length > 1) {
                        stream((String[]) ArrayUtils.subarray(args, 1, args.length), console);
                    } else {
                        console.println("Specify url to stream from, and optionally the sink(s) to use");
                    }
                    return;
                case SUBCMD_SOURCES:
                    listSources(console);
                    return;
                case SUBCMD_SINKS:
                    listSinks(console);
                    return;
                case SUBCMD_GROUPS:
                    handleGroups(args, console);
                default:
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private static final String SUBCMD_CREATE = "create";
    private static final String SUBCMD_REMOVE = "remove";
    private static final String SUBCMD_ADD = "add";
    private static final String SUBCMD_CLEAR = "clear";

    private void handleGroups(String[] args, Console console) {
        if (args.length == 1) {
            if (this.audioManager.getGroups().size() > 0) {
                for (String group : this.audioManager.getGroups()) {
                    console.println("Audio sink group " + group);
                    if (audioManager.getGroup(group).size() > 0) {
                        for (String sink : audioManager.getGroup(group)) {
                            console.println(sink);
                        }
                    } else {
                        console.println("No audio sinks found.");
                    }
                }
            } else {
                console.println("No audio sink groups found.");
            }
        } else if (args.length > 1) {
            String subCommand = args[1];
            switch (subCommand) {
                case SUBCMD_CREATE: {
                    if (args.length > 2) {
                        this.audioManager.createGroup(args[2]);
                    }
                    return;
                }
                case SUBCMD_REMOVE: {
                    if (args.length > 3) {
                        this.audioManager.removeFromGroup(args[3], args[2]);
                    }
                    return;
                }
                case SUBCMD_ADD: {
                    if (args.length > 3) {
                        this.audioManager.addToGroup(args[3], args[2]);
                    }
                    return;
                }
                case SUBCMD_CLEAR: {
                    if (args.length > 2) {
                        this.audioManager.removeGroup(args[2]);
                    }
                    return;
                }
                default:
                    break;
            }
        }
    }

    private void listSources(Console console) {
        if (audioManager.getSourceIds().size() > 0) {
            for (String source : audioManager.getSourceIds()) {
                console.println(source);
            }
        } else {
            console.println("No audio sources found.");
        }
    }

    private void listSinks(Console console) {
        if (audioManager.getSinkIds().size() > 0) {
            for (String sink : audioManager.getSinkIds()) {
                console.println(sink);
            }
        } else {
            console.println("No audio sinks found.");
        }
    }

    private void play(String[] args, Console console) {
        if (args.length == 1) {
            try {
                audioManager.playFile(args[0]);
            } catch (AudioException e) {
                console.println(e.getMessage());
            }
        } else if (args.length == 2) {
            Set<String> sinks = audioManager.getSinks(args[0]);
            for (String aSink : sinks) {
                try {
                    audioManager.playFile(args[1], aSink);
                } catch (AudioException e) {
                    console.println(e.getMessage());
                }
            }

            Set<String> groups = audioManager.getGroups(args[0]);
            for (String aGroup : groups) {
                try {
                    audioManager.playFile(args[1], aGroup);
                } catch (AudioException e) {
                    console.println(e.getMessage());
                }
            }
        }
    }

    private void stream(String[] args, Console console) {
        if (args.length == 1) {
            try {
                audioManager.stream(args[0]);
            } catch (AudioException e) {
                console.println(e.getMessage());
            }
        } else if (args.length == 2) {
            Set<String> sinks = audioManager.getSinks(args[0]);
            for (String aSink : sinks) {
                try {
                    audioManager.stream(args[1], aSink);
                } catch (AudioException e) {
                    console.println(e.getMessage());
                }
            }

            Set<String> groups = audioManager.getGroups(args[0]);
            for (String aGroup : groups) {
                try {
                    audioManager.stream(args[1], aGroup);
                } catch (AudioException e) {
                    console.println(e.getMessage());
                }
            }
        }
    }

    protected void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        this.audioManager = null;
    }

}
