/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bosesoundtouch10.helper;

/**
 * The {@link SimpleCallBackInterface} interface is a interface for callback from the websocketlistenere to the
 * BoseSoundTouch10Handler to update the UI or signal errors on the websocket.
 *
 * @author syracom - Initial contribution
 */
public interface SimpleCallBackInterface {

    public void refreshUI(String message);

    public void setStatusOffline();
}
