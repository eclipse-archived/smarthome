/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bosesoundtouch10.helper;

import org.eclipse.smarthome.binding.bosesoundtouch10.handler.BoseSoundTouch10Handler.BSTKeys;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link WSHelperInterface} class defines methods to be implemented to communicate with the REST API of the
 * speaker.
 *
 * @author syracom - Initial contribution
 */
public interface WSHelperInterface {

    public String pressAndReleaseButtonOnSpeaker(BSTKeys keyIdentifier);

    public String selectAUX();

    public String selectBluetooth();

    public String setVolume(PercentType num);

    public String setBass(DecimalType num);

    public String get(String service);

}
