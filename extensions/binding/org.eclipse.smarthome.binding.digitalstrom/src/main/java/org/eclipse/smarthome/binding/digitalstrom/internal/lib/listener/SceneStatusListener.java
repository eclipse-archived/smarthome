/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;

/**
 * The {@link SceneStatusListener} is notified, if a {@link InternalScene} status has changed or a
 * {@link InternalScene} has been removed or added.
 *
 * <p>
 * By implementation with the id {@link #SCENE_DISCOVERY} this listener can be used as a scene discovery to get
 * informed, if a new {@link InternalScene}s is added or removed from the digitalSTROM-System.<br>
 * For that the {@link SceneStatusListener} has to be registered on the
 * {@link SceneManager#registerSceneListener(SceneStatusListener)}. Then the {@link SceneStatusListener} gets
 * informed by the methods {@link #onSceneAdded(InternalScene)} and {@link #onSceneRemoved(InternalScene)}.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface SceneStatusListener {

    public final static String SCENE_DISCOVERY = "SceneDiscovery";

    /**
     * This method is called whenever the state of the {@link InternalScene} has changed.
     *
     * @param newState (true = call | false = undo)
     */
    public void onSceneStateChanged(boolean newState);

    /**
     * This method is called whenever a {@link InternalScene} is removed.
     *
     * @param scene
     */
    public void onSceneRemoved(InternalScene scene);

    /**
     * This method is called whenever a {@link InternalScene} is added.
     *
     * @param scene
     */
    public void onSceneAdded(InternalScene scene);

    /**
     * Return the id of this {@link SceneStatusListener}.
     *
     * @return the scene listener id
     */
    public String getSceneStatusListenerID();
}
