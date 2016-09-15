/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.io.audio.AudioManager;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * Console command extension for all audio features.
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class AudioConsoleCommandExtension extends AbstractConsoleCommandExtension {

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
                buildCommandUsage(SUBCMD_PLAY + " <sink> <filename>",
                        "plays a sound file from the sounds folder through the optionally specified audio sink(s)"),
                buildCommandUsage(SUBCMD_STREAM + " <sink> <url>",
                        "streams the sound from the url through the optionally specified audio sink(s)"),
                buildCommandUsage(SUBCMD_SOURCES, "lists the audio sources"),
                buildCommandUsage(SUBCMD_SINKS, "lists the audio sinks") });

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
                default:
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void listSources(Console console) {
        if (audioManager.getSources().size() > 0) {
            for (String source : audioManager.getSources()) {
                console.println(source);
            }
        } else {
            console.println("No audio sources found.");
        }
    }

    private void listSinks(Console console) {
        if (audioManager.getSinks().size() > 0) {
            for (String sink : audioManager.getSinks()) {
                console.println(sink);
            }
        } else {
            console.println("No audio sinks found.");
        }
    }

    private void play(String[] args, Console console) {
        if (args.length == 1) {
            audioManager.play(args[0]);
        } else if (args.length == 2) {
            Set<String> sinks = audioManager.getSinks(args[0]);
            for (String aSink : sinks) {
                audioManager.play(args[1], aSink);
            }
        }
    }

    private void stream(String[] args, Console console) {
        if (args.length == 1) {
            audioManager.stream(args[0]);
        } else if (args.length == 2) {
            Set<String> sinks = audioManager.getSinks(args[0]);
            for (String aSink : sinks) {
                audioManager.stream(args[1], aSink);
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
