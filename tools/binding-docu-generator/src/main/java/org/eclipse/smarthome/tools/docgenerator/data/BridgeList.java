package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Bridge;

public class BridgeList extends ModelList {
    /**
     * @return Returns a new {@link Bridge} object.
     */
    @Override
    public Bridge getNewModel() {
        return new Bridge();
    }
}
