package org.eclipse.smarthome.model.thing.test.hue;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import com.google.common.collect.Lists;

/**
 *
 * {@link TestHueChannelTypeProvider} is used for the model thing tests to provide channel types.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class TestHueChannelTypeProvider implements ChannelTypeProvider {

    public static final ChannelTypeUID COLORX_TEMP_CHANNEL_TYPE_UID = new ChannelTypeUID("Xhue:Xcolor_temperature");
    public static final ChannelTypeUID COLORX_CHANNEL_TYPE_UID = new ChannelTypeUID("Xhue:color");
    public static final ChannelTypeUID COLOR_TEMP_CHANNEL_TYPE_UID = new ChannelTypeUID("hue:color_temperature");
    public static final ChannelTypeUID COLOR_CHANNEL_TYPE_UID = new ChannelTypeUID("hue:color");

    private List<ChannelType> channelTypes;

    public TestHueChannelTypeProvider() {
        try {
            ChannelType ctColor = new ChannelType(COLOR_CHANNEL_TYPE_UID, false, "Color", "colorLabel", "description",
                    null, null, null, new URI("hue", "LCT001:color", null));
            ChannelType ctColorTemperature = new ChannelType(COLOR_TEMP_CHANNEL_TYPE_UID, false, "Dimmer",
                    "colorTemperatureLabel", "description", null, null, null,
                    new URI("hue", "LCT001:color_temperature", null));
            ChannelType ctColorX = new ChannelType(COLORX_CHANNEL_TYPE_UID, false, "Color", "colorLabel", "description",
                    null, null, null, new URI("Xhue", "XLCT001:Xcolor", null));
            ChannelType ctColorTemperatureX = new ChannelType(COLORX_TEMP_CHANNEL_TYPE_UID, false, "Dimmer",
                    "colorTemperatureLabel", "description", null, null, null,
                    new URI("Xhue", "XLCT001:Xcolor_temperature", null));
            channelTypes = Lists.newArrayList(ctColor, ctColorTemperature, ctColorX, ctColorTemperatureX);
        } catch (Exception willNeverBeThrown) {
        }
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return Lists.newArrayList();
    }

}
