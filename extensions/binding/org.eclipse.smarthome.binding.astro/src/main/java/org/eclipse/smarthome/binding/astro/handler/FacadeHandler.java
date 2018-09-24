package org.eclipse.smarthome.binding.astro.handler;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.astro.internal.config.FacadeThingConfig;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FacadeHandler is responsible for updating calculated facade illumination data
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FacadeHandler extends BaseThingHandler implements AstroHandlerListener {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_FACADE));

    private static final State RIGHT = new StringType("RIGHT");
    private static final State LEFT = new StringType("LEFT");
    private static final State FRONT = new StringType("FRONT");

    private State previousInWindow = UnDefType.UNDEF;
    private State previousRelative = UnDefType.UNDEF;

    @NonNullByDefault({})
    private FacadeThingConfig config;

    int shift;

    public FacadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        config = getConfigAs(FacadeThingConfig.class);
        shift = getShift(config.getOrientation(), config.getNegativeOffset(), config.getPositiveOffset());

        Bridge bridge = getBridge();
        initializeBridge(bridge != null ? bridge.getHandler() : null, bridge != null ? bridge.getStatus() : null);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());

        Bridge bridge = getBridge();
        initializeBridge(bridge != null ? bridge.getHandler() : null, bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, @Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                AstroThingHandler bridgeHandler = (AstroThingHandler) thingHandler;
                bridgeHandler.addListener(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AstroThingHandler bridgeHandler = (AstroThingHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                bridgeHandler.removeListener(this);
            }
        }
    }

    @Override
    public void publishPlanet(@Nullable Planet planet) {
        if (planet instanceof Sun) {
            Sun sun = (Sun) planet;
            float sunAzimut = sun.getPosition().getAzimuth().floatValue();
            float bearing = getBearing(config.getOrientation(), config.getNegativeOffset(), config.getPositiveOffset(),
                    sunAzimut, shift);

            State sunInWindow = bearing != 0 ? OnOffType.ON : OnOffType.OFF;
            State relativePosition = getRelativePosition(config.getOrientation(), config.getMargin(), sunAzimut);

            String event = (previousInWindow == OnOffType.ON && sunInWindow == OnOffType.OFF) ? EVENT_LEAVE_FACADE
                    : (previousInWindow == OnOffType.OFF && sunInWindow == OnOffType.ON) ? EVENT_ENTER_FACADE
                            : (relativePosition == FRONT && previousRelative != FRONT
                                    && previousRelative != UnDefType.UNDEF) ? EVENT_FRONT_FACADE : null;

            getThing().getChannels().forEach(channel -> {
                switch (channel.getUID().getId()) {
                    case CHANNEL_ID_FACADE_FACING:
                        updateState(channel.getUID(), sunInWindow);
                        break;
                    case CHANNEL_ID_FACADE_BEARING:
                        updateState(channel.getUID(), new QuantityType<>(bearing, SmartHomeUnits.PERCENT));
                        break;
                    case CHANNEL_ID_FACADE_SIDE:
                        updateState(channel.getUID(), relativePosition);
                        break;
                    case EVENT_CHANNEL_ID_FACADE:
                        if (event != null) {
                            triggerChannel(channel.getUID(), event);
                        }
                }
            });
            previousInWindow = sunInWindow;
            previousRelative = relativePosition;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge must be an Astro Sun thing");
        }
    }

    private float getBearing(int orientation, int nOffset, int pOffset, float sunAzimut, int shift) {
        return (sunAzimut >= orientation && sunAzimut <= orientation + pOffset)
                ? 100 * (orientation + pOffset - sunAzimut + 2 * shift) / pOffset
                : (sunAzimut < orientation && sunAzimut >= orientation - nOffset)
                        ? 100 * (sunAzimut - (orientation - nOffset) + 2 * shift) / nOffset
                        : 0;
    }

    private int getShift(int orientation, int nOffset, int pOffset) {
        int shift = (orientation - nOffset) < 0 ? (orientation - nOffset) * -1
                : (orientation + pOffset) > 360 ? 360 - (orientation + pOffset) : 0;
        return shift;
    }

    private State getRelativePosition(int orientation, int margin, float sunAzimut) {
        State relativePosition = sunAzimut < (orientation - margin) ? LEFT
                : sunAzimut > (orientation + margin) ? RIGHT : FRONT;
        return relativePosition;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // Nothing to do here
    }

}
