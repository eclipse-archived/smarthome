/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * An {@link PlayerItem} allows to control a player, e.g. an audio player.
 *
 * @author Alex Tugarev
 */
public class PlayerItem extends GenericItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();

    static {
        acceptedDataTypes.add(PlayPauseType.class);
        acceptedDataTypes.add(RewindFastforwardType.class);
        acceptedDataTypes.add(UnDefType.class);

        acceptedCommandTypes.add(PlayPauseType.class);
        acceptedCommandTypes.add(RewindFastforwardType.class);
        acceptedCommandTypes.add(NextPreviousType.class);
        acceptedCommandTypes.add(RefreshType.class);
    }

    public PlayerItem(@NonNull String name) {
        super(CoreItemFactory.PLAYER, name);
    }

    /* package */ PlayerItem(@NonNull String type, @NonNull String name) {
        super(type, name);
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return Collections.unmodifiableList(acceptedDataTypes);
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return Collections.unmodifiableList(acceptedCommandTypes);
    }

    public void send(PlayPauseType command) {
        internalSend(command);
    }

    public void send(RewindFastforwardType command) {
        internalSend(command);
    }

    public void send(NextPreviousType command) {
        internalSend(command);
    }

    @Override
    public void setState(State state) {
        if (isAcceptedState(acceptedDataTypes, state)) {
            super.setState(state);
        } else {
            logSetTypeError(state);
        }
    }

}
