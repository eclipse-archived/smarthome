/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lwm2m.old;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Lwm2mObjectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Channels will have some properties:
 * - "unit": for the lwm2m color type this indicates the color space ("RGB", "HSB")
 *
 * @author David Graeff - Initial contribution
 */
public class Lwm2mObjectHandler implements ObservationListener {

    private static final int UNITS_RESOURCE = 5701;
    private static final String BINDING_ID = null;
    private Logger logger = LoggerFactory.getLogger(Lwm2mObjectHandler.class);
    private final LeshanOpenhab leshan;
    public final LwM2mPath path;
    public final ObjectModel objectModel;
    public String unit;
    private Observation observe;
    private LwM2mObjectInstance objectNode;
    private Thing thing;
    private Registration client;

    public Lwm2mObjectHandler(Thing thing, LeshanOpenhab leshan, Registration client, LwM2mPath path) {
        this.leshan = leshan;
        this.path = path;
        this.client = client;
        objectModel = leshan.getObjectModel(client, path.getObjectId());
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        int resID = Integer.valueOf(channelUID.getId());
        ResourceModel resourceModel = objectModel.resources.get(resID);
        if (resourceModel == null) {
            logger.error("Resource model not found for given res id %i. %s", resID, thing.getUID().getAsString());
            return;
        }
        try {
            leshan.requestChange(client, path, unit, resourceModel, command);
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private void updateStatus(ThingStatus offline, ThingStatusDetail communicationError, String localizedMessage) {
    }

    private void updateStatus(ThingStatus initializing) {
    }

    private void updateState(String id2, State newState) {
    }

    private ThingBuilder editThing() {
        return null;
    }

    private void updateThing(Thing build) {
    }

    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);
        try {
            observe = leshan.startObserve(client, path, this);
            objectNode = leshan.requestValues(client, path);
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return;
        }
        updateLwM2mNode(objectNode);
        updateStatus(ThingStatus.ONLINE);
    }

    public void dispose() {
        if (observe != null) {
            leshan.stopObserve(observe, this);
        }
    }

    public void updateLwM2mNode(LwM2mObjectInstance value) {
        LwM2mObjectInstance objectInstance = value;
        Map<Integer, LwM2mResource> resources = objectInstance.getResources();
        LwM2mResource lwM2mResource = resources.get(UNITS_RESOURCE);
        if (lwM2mResource != null) {
            unit = (String) lwM2mResource.getValue();
        }

        updatedChannels.clear();
        for (LwM2mResource resource : resources.values()) {
            updateResource(resource);
        }
        removeChannelsNotUpdated();
        updatedChannels.clear();
    }

    private void removeChannelsNotUpdated() {
        List<Channel> channels = thing.getChannels();
        int channelCount = channels.size();
        for (Iterator<Channel> iterator = channels.iterator(); iterator.hasNext();) {
            Channel channel = iterator.next();
            if (!updatedChannels.contains(Lwm2mUID.getResourceID(channel.getUID()))) {
                iterator.remove();
            }
        }
        if (channelCount != channels.size()) {
            logger.debug("Remove unused channels in %s", thing.getUID().getAsString());
            updateThing(editThing().withChannels(channels).build());
        }
    }

    private Set<Integer> updatedChannels = new TreeSet<>();

    private void updateResource(LwM2mResource resource) {
        if (resource.getId() == UNITS_RESOURCE) {
            return;
        }

        if (!resource.isMultiInstances()) {
            updateResourceInstance(resource, -1);
        } else {
            for (Integer instanceID : resource.getValues().keySet()) {
                updateResourceInstance(resource, instanceID);
            }
        }
    }

    private void updateResourceInstance(LwM2mResource resource, int resourceInstanceID) {
        String channelID = Lwm2mUID.getChannelID(resource, resourceInstanceID);
        Channel channel = thing.getChannel(channelID);
        updatedChannels.add(resource.getId());

        if (channel == null) {
            ChannelType channelType = TypeResolver.resolve(new ChannelTypeUID(BINDING_ID, channelID));

            if (channelType == null) {
                logger.debug("Add new channel in %s FAILED: %s. No xml type found", thing.getUID().getAsString(),
                        channelID);
                return;
            }

            ChannelBuilder channelBuilder = ChannelBuilder
                    .create(Lwm2mUID.createChannelUID(thing.getUID(), channelID), channelType.getItemType())
                    .withType(channelType.getUID()).withDefaultTags(channelType.getTags())
                    .withLabel(channelType.getLabel()).withDescription(channelType.getDescription());

            channel = channelBuilder.build();
            logger.debug("Add new channel in %s: %s", thing.getUID().getAsString(), channelType.getLabel());
            updateThing(editThing().withChannel(channel).build());
        }

        updateChannelWithResValue(channel, resource.getType(),
                resource.isMultiInstances() ? resource.getValue(resourceInstanceID) : resource.getValue());
    }

    /**
     *
     * @param resource The lwm2m resource.
     * @param resourceInstanceID A resource may have multiple instances. Usually this is 0 for the first instance.
     * @return Return a new channel, if there was no channel found in this thing for the resource.
     */
    private void updateChannelWithResValue(Channel channel, Type type, Object value) {
        State newState;
        switch (channel.getAcceptedItemType()) {
            case "DateTime":
                if (type != ResourceModel.Type.STRING) {
                    logger.warn("String expected!");
                    return;
                }
                try {
                    newState = new DateTimeType((String) value);
                } catch (IllegalArgumentException e) {
                    logger.warn("DateTime format unknown: " + (String) value);
                    return;
                }
                break;
            case "Switch":
                switch (type) {
                    case BOOLEAN:
                        newState = ((Boolean) value == true) ? OnOffType.ON : OnOffType.OFF;
                        break;
                    case INTEGER:
                        newState = ((Integer) value > 0) ? OnOffType.ON : OnOffType.OFF;
                        break;
                    default:
                        logger.warn("Number or Boolean expected!");
                        return;
                }
                break;
            case "Number":
                switch (type) {
                    case FLOAT:
                        newState = new DecimalType((Float) value);
                        break;
                    case INTEGER:
                        newState = new DecimalType((Integer) value);
                        break;
                    default:
                        logger.warn("Number expected!");
                        return;
                }
                break;
            case "Rollershutter":
                logger.warn("Rollershutter not supported yet!");
                return;
            case "String":
                if (type != ResourceModel.Type.STRING) {
                    logger.warn("String expected!");
                    return;
                }
                newState = new StringType((String) value);
                break;
            case "Color":
                switch (unit) {
                    case "RGB":
                        String[] rgb = ((String) value).split(",");
                        if (rgb.length != 3) {
                            throw new IllegalArgumentException(
                                    String.format("RGB format expected: r,g,b. Value: %s", (String) value));
                        }
                        newState = HSBType.fromRGB(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]),
                                Integer.valueOf(rgb[2]));
                        break;
                    case "HSB":
                        newState = new HSBType((String) value);
                        break;
                    default:
                        logger.warn("Unsupported color space unit: %s", unit);
                        return;
                }
                break;
            default:
                logger.warn("ItemType not supported: %s", channel.getAcceptedItemType());
                return;
        }
        updateState(channel.getUID().getId(), newState);
    }

    @Override
    public void cancelled(Observation observation) {
    }

    /**
     * If Objects / Object instances change on the client, it will inform the server and we get called back
     * by the bridge handler which is responsible for the given lwm2m client.
     *
     * We check if this openhab thing ( == lwm2m object instance) is still part of the client, if not, we remove this
     * thing from the ThingRegistry.
     *
     * @param clientUpdated
     */
    public void updateClient(Registration clientUpdated) {
        this.client = clientUpdated;
        LwM2mPath[] objectPaths = leshan.getObjectLinks(client);
        boolean found = false;
        for (LwM2mPath objectPath : objectPaths) {
            if (objectPath.equals(path)) {
                found = true;
                break;
            }
        }

        // TODO
    }

    @Override
    public void newObservation(Observation observation, Registration registration) {
    }

    @Override
    public void onResponse(Observation observation, Registration registration, ObserveResponse response) {
        if (!path.equals(observation.getPath())) {
            return;
        }

        LwM2mNode value = response.getContent();

        if (value instanceof LwM2mObjectInstance) {
            updateLwM2mNode((LwM2mObjectInstance) value);
        } else if (value instanceof LwM2mResource) {
            updateResource((LwM2mResource) value);
        }
    }

    @Override
    public void onError(Observation observation, Registration registration, Exception error) {
    }
}
