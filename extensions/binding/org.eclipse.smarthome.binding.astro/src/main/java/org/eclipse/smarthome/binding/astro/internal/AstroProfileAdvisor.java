package org.eclipse.smarthome.binding.astro.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.astro.AstroBindingConstants;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@NonNullByDefault
@Component
public class AstroProfileAdvisor implements ProfileAdvisor {

    private static final String RANGE_EVENT = "rangeEvent";
    private static final ChannelTypeUID CHANNEL_TYPE_RANGE_EVENT_UID = new ChannelTypeUID(
            AstroBindingConstants.BINDING_ID, RANGE_EVENT);

    @NonNullByDefault({})
    private ChannelTypeRegistry channelTypeRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    @Nullable
    @Override
    public ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
        return getSuggestedProfileTypeUID(channelType, itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@NonNull ChannelType channelType,
            @Nullable String itemType) {
        switch (channelType.getKind()) {
            case STATE:
                return SystemProfiles.DEFAULT;
            case TRIGGER:
                if (CHANNEL_TYPE_RANGE_EVENT_UID.equals(channelType.getUID())) {
                    return SystemProfiles.RAWBUTTON_TOGGLE_SWITCH;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported channel kind: " + channelType.getKind());
        }
        return null;
    }

}
