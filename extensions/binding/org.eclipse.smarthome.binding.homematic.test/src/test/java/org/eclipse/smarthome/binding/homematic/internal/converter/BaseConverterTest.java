package org.eclipse.smarthome.binding.homematic.internal.converter;

import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.junit.Before;

public class BaseConverterTest {

    protected final HmDatapoint floatDp = new HmDatapoint("floatDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint integerDp = new HmDatapoint("integerDp", "", HmValueType.INTEGER, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint floatQuantityDp = new HmDatapoint("floatQuantityDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    protected final HmDatapoint integerQuantityDp = new HmDatapoint("floatIntegerDp", "", HmValueType.INTEGER, null,
            false, HmParamsetType.VALUES);

    @Before
    public void setup() {
        HmChannel stubChannel = new HmChannel("stubChannel", 0);
        stubChannel.setDevice(new HmDevice("LEQ123456", HmInterface.RF, "HM-STUB-DEVICE", "", "", ""));
        floatDp.setChannel(stubChannel);
    }

}
