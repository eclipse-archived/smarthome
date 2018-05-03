package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.StateUpdate;

@NonNullByDefault
public interface HueClient {

    boolean registerLightStatusListener(LightStatusListener lightStatusListener);

    boolean unregisterLightStatusListener(LightStatusListener lightStatusListener);

    @Nullable
    FullLight getLightById(String lightId);

    void updateLightState(FullLight light, StateUpdate stateUpdate);

}
