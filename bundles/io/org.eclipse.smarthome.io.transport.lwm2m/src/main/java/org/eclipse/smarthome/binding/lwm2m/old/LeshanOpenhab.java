package org.eclipse.smarthome.binding.lwm2m.old;

import java.util.Dictionary;
import java.util.concurrent.TimeoutException;

import javax.naming.CommunicationException;

import org.eclipse.leshan.Link;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.DownlinkRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.impl.InMemorySecurityStore;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StandardModelProvider;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LeshanOpenhab {
    private static final long TIMEOUT = 5000; // ms

    private Logger logger = LoggerFactory.getLogger(LeshanOpenhab.class);
    private LeshanServer lwServer;
    private final Gson gson;
    private LwM2MClientDiscovery discover = new LwM2MClientDiscovery();

    public LeshanOpenhab() {
        // Register a node deserializer
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeDeserializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    /**
     * Start the Leshan server. The key-value properties parameter is used to obtain the ports, and security parameters.
     *
     * @param properties Obtain the port (lwm2m_port), the secure port ("lwm2m_port_secure"), if we want to use elliptic
     *            curves ("lwm2m_secure_use_ecc") and the key, elliptic curve x,y parameters ("lwm2m_secure_public_key",
     *            "lwm2m_secure_point_x",
     *            "lwm2m_secure_point_y").
     * @throws Exception
     */
    public void createAndStartServer(Dictionary<String, Object> properties) throws Exception {
        String localAddress = null;
        int localPort = 1;
        int secureLocalPort = 2;

        // Prepare LWM2M server
        LeshanServerBuilder builder = new LeshanServerBuilder();
        builder.setLocalAddress(localAddress, localPort);
        builder.setLocalSecureAddress(localAddress, secureLocalPort);
        builder.setSecurityStore(new InMemorySecurityStore());
        // TODO Waiting for connector factories for dtls support
        // builder.setConnectorFactory(new ConnectorFactory() {...});

        LwM2mModelProvider modelProvider = new StandardModelProvider();
        builder.setObjectModelProvider(modelProvider);

        lwServer = builder.build();
        lwServer.start();
    }

    public void stopServer() {
        if (lwServer == null) {
            return;
        }
        lwServer.stop();
        lwServer.destroy();
        lwServer = null;
    }

    /**
     * Start observing an lwm2m object instance. This should be called by a thing handler during initialization
     * to be notified of resource changes.
     *
     * @param ObjectInstance The instance id of the object, 0 for single instance objects.
     * @param listener A listener, usually the thing handler.
     * @return Return an Observation object. Use this to call stopObserve().
     * @throws InterruptedException
     */
    public Observation startObserve(Registration client, LwM2mPath id, ObservationListener listener)
            throws InterruptedException {
        ObserveRequest request = new ObserveRequest(id.getObjectId(), id.getObjectInstanceId());
        ObserveResponse cResponse = lwServer.send(client, request, TIMEOUT);
        if (cResponse == null) {
            logger.warn(String.format("startObserve failed for %i/%i", id.getObjectId(), id.getObjectInstanceId()));
        } else {
            String response = gson.toJson(cResponse);
            if (cResponse.getCode().isError()) {
                logger.warn("Response indicate error '%s'", cResponse.getErrorMessage());
            } else {
                logger.debug("Response %s", response);
            }
            lwServer.getObservationService().addListener(listener);
            return cResponse.getObservation();
        }
        return null;
    }

    /**
     * Stop observing an object instance.
     *
     * @param observation The Observation object, you got by calling startObserve().
     * @param listener The listener, you registered by calling startObserve().
     */
    public void stopObserve(Observation observation, ObservationListener listener) {
        lwServer.getObservationService().removeListener(listener);
        lwServer.getObservationService().cancelObservation(observation);
    }

    /**
     * Stop all observations for the given client.
     *
     * @param client
     */
    public void stopObserve(Registration client) {
        lwServer.getObservationService().cancelObservations(client);
    }

    // The coap url /endpoint/objectId/instanceId maps to /bridgeID/thingTypeID/thingID
    public void requestChange(Registration client, LwM2mPath path, String unit, ResourceModel resource, Command command)
            throws InterruptedException, TimeoutException {

        DownlinkRequest<?> request = null;

        int resourceID = resource.id;
        int objectID = path.getObjectId();
        int objectIDinstance = path.getObjectInstanceId();

        if (resource.operations.isExecutable()) {
            request = new ExecuteRequest(objectID, objectIDinstance, resourceID);
        } else {
            if (command instanceof StringType) {
                request = new WriteRequest(objectID, objectIDinstance, resourceID, ((StringType) command).toString());
            } else if (command instanceof PointType) {
                request = new WriteRequest(objectID, objectIDinstance, resourceID, ((PointType) command).toString());
            } else if (command instanceof HSBType) {
                HSBType v = (HSBType) command;
                String color;
                switch (unit) {
                    case "RGB":
                        color = String.valueOf(v.getRed()) + "," + String.valueOf(v.getGreen()) + ","
                                + String.valueOf(v.getRed());
                        break;
                    case "HSV":
                        color = v.toString();
                        break;
                    default:
                        logger.warn(
                                "Colorspace unknown. The lwm2m device must support RGB or HSV and propagate this via the \"unit\" field of the resource.");
                        return;
                }
                request = new WriteRequest(objectID, objectIDinstance, resourceID, color);
            } else if (command instanceof Enum) {
                int value = ((Enum<?>) command).ordinal();
                switch (resource.type) {
                    case BOOLEAN:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID, value > 0);
                        break;
                    case INTEGER:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID, value);
                        break;
                    case OPAQUE:
                    case STRING:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID, ((Enum<?>) command).name());
                    default:
                        logger.warn("Invalid type. Must be Boolean, Integer or String");
                        return;

                }
            } else if (command instanceof DecimalType) {
                switch (resource.type) {
                    case BOOLEAN:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID,
                                ((DecimalType) command).intValue() > 0);
                        break;
                    case FLOAT:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID,
                                ((DecimalType) command).floatValue());
                        break;
                    case INTEGER:
                        request = new WriteRequest(objectID, objectIDinstance, resourceID,
                                ((DecimalType) command).intValue());
                        break;
                    default:
                        logger.warn("Invalid number. Must be Integer");
                        return;

                }
            }
        }

        LwM2mResponse cResponse = lwServer.send(client, request, TIMEOUT);
        if (cResponse == null) {
            throw new TimeoutException(
                    String.format("Request %i/%i/%i timed out.", objectID, objectIDinstance, resourceID));
        } else {
            String response = this.gson.toJson(cResponse);
            if (cResponse.getCode().isError()) {
                logger.warn("Response indicate error '%s'", cResponse.getErrorMessage());
            } else {
                logger.debug("Response %s", response);
            }
        }
    }

    public ObjectModel getObjectModel(Registration client, int objectID) {
        return lwServer.getModelProvider().getObjectModel(client).getObjectModel(objectID);
    }

    public Registration getClient(String endpoint) {
        return lwServer.getRegistrationService().getByEndpoint(endpoint);
    }

    public LwM2mObjectInstance requestValues(Registration client, LwM2mPath path)
            throws InterruptedException, CommunicationException {
        ReadRequest readRequest = new ReadRequest(path.getObjectId(), path.getObjectInstanceId());
        ReadResponse readResponse = lwServer.send(client, readRequest);
        if (readResponse.isFailure()) {
            throw new CommunicationException(String.format("requestValues failed: %s", readResponse.getErrorMessage()));
        }
        LwM2mNode content = readResponse.getContent();
        if (!(content instanceof LwM2mObjectInstance)) {
            throw new CommunicationException("requestValues expected object instance");
        }
        return (LwM2mObjectInstance) content;
    }

    public void startDiscovery(BundleContext bundleContext) {
        discover.start(bundleContext, lwServer.getRegistrationService().getAllRegistrations());
        lwServer.getRegistrationService().addListener(discover);
    }

    public void stopDiscovery() {
        // discover.stop();
    }

    public LwM2mPath[] getObjectLinks(Registration client) {
        Link[] objectLinks = client.getObjectLinks();
        LwM2mPath[] objects = new LwM2mPath[objectLinks.length];
        for (int i = 0; i < objectLinks.length; i++) {
            Link linkObject = objectLinks[i];
            objects[i] = new LwM2mPath(linkObject.getUrl());
        }
        return objects;
    }

    public void startClientObserve(RegistrationListener listener) {
        lwServer.getRegistrationService().addListener(listener);
    }

    public void stopClientObserve(RegistrationListener listener) {
        lwServer.getRegistrationService().removeListener(listener);
    }
}
