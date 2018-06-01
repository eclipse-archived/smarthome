/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bosesoundtouch.internal;

/**
 * The {@link AvailableSources} is used to find out, which sources and functions are available
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public interface AvailableSources {

    public boolean isBluetoothAvailable();

    public boolean isAUXAvailable();

    public boolean isAUX1Available();

    public boolean isAUX2Available();

    public boolean isAUX3Available();

    public boolean isTVAvailable();

    public boolean isHDMI1Available();

    public boolean isInternetRadioAvailable();

    public boolean isStoredMusicAvailable();

    public boolean isBassAvailable();

    public void setAUXAvailable(boolean aux);

    public void setAUX1Available(boolean aux1);

    public void setAUX2Available(boolean aux2);

    public void setAUX3Available(boolean aux3);

    public void setStoredMusicAvailable(boolean storedMusic);

    public void setInternetRadioAvailable(boolean internetRadio);

    public void setBluetoothAvailable(boolean bluetooth);

    public void setTVAvailable(boolean tv);

    public void setHDMI1Available(boolean hdmi1);

    public void setBassAvailable(boolean bass);
}
