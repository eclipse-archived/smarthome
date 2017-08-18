package org.openhab.binding.lwm2mleshan.internal;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.KeySpec;
import java.util.Dictionary;
import java.util.concurrent.TimeoutException;

import javax.naming.CommunicationException;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
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
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.impl.SecurityRegistryImpl;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StandardModelProvider;
import org.eclipse.leshan.server.observation.ObservationRegistryListener;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.eclipse.leshan.util.Hex;
import org.eclipse.smarthome.binding.lwm2m.internal.discovery.LwM2MClientDiscovery;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lwm2mleshan.handler.Lwm2mObjectHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LeshanOpenhab {
    private static final long TIMEOUT = 5000; // ms

    private Logger logger = LoggerFactory.getLogger(Lwm2mObjectHandler.class);
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

    private static byte[] getOrDefault(String in, String defaultV) {
        return Hex.decodeHex((in == null || in.length() == 0) ? defaultV.toCharArray() : in.toCharArray());
    }

    private static int getOrDefault(Integer in, int defaultV) {
        return in == null ? defaultV : in;
    }

    private static boolean getOrDefault(Boolean in, boolean defaultV) {
        return in == null ? defaultV : in;
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
        int localPort = getOrDefault((Integer) properties.get("lwm2m_port"), LeshanServerBuilder.PORT);
        int secureLocalPort = getOrDefault((Integer) properties.get("lwm2m_port_secure"),
                LeshanServerBuilder.PORT_DTLS);
        boolean useECC = getOrDefault((Boolean) properties.get("lwm2m_secure_use_ecc"), false);
        String temp;
        temp = (String) properties.get("lwm2m_secure_public_key");
        byte[] privateKeyPart = getOrDefault(temp, "1dae121ba406802ef07c193c1ee4df91115aabd79c1ed7f4c0ef7ef6a5449400");
        temp = (String) properties.get("lwm2m_secure_point_x");
        byte[] publicX = getOrDefault(temp, "fcc28728c123b155be410fc1c0651da374fc6ebe7f96606e90d927d188894a73");
        temp = (String) properties.get("lwm2m_secure_point_y");
        byte[] publicY = getOrDefault(temp, "d2ffaa73957d76984633fc1cc54d0b763ca0559a9dff9706e9f4557dacc3f52a");

        // Prepare LWM2M server
        LeshanServerBuilder builder = new LeshanServerBuilder();
        builder.setLocalAddress(localAddress, localPort);
        builder.setLocalSecureAddress(localAddress, secureLocalPort);

        // Get public and private server key
        PrivateKey privateKey = null;
        PublicKey publicKey = null;

        if (useECC) {
            // Get Elliptic Curve Parameter spec for secp256r1
            AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
            algoParameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

            // Create key specs
            KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(publicY)),
                    parameterSpec);
            KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateKeyPart), parameterSpec);

            // Get keys
            publicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
            privateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);

            builder.setSecurityRegistry(new SecurityRegistryImpl(privateKey, publicKey));
        }

        LwM2mModelProvider modelProvider = new StandardModelProvider();
        builder.setObjectModelProvider(modelProvider);

        lwServer = builder.build();
        if (!useECC) {
            SecurityInfo info1 = SecurityInfo.newPreSharedKeyInfo("lwm2mClient1", "identity1", privateKeyPart);
            lwServer.getSecurityRegistry().add(info1);
        }
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
    public Observation startObserve(ObjectInstance id, ObservationRegistryListener listener)
            throws InterruptedException {
        ObserveRequest request = new ObserveRequest(id.getObjectID(), id.getInstanceID());
        ObserveResponse cResponse = lwServer.send(id.getClient(), request, TIMEOUT);
        if (cResponse == null) {
            logger.warn(String.format("startObserve failed for %i/%i", id.getObjectID(), id.getInstanceID()));
        } else {
            String response = gson.toJson(cResponse);
            if (cResponse.getCode().isError()) {
                logger.warn("Response indicate error '%s'", cResponse.getErrorMessage());
            } else {
                logger.debug("Response %s", response);
            }
            lwServer.getObservationRegistry().addListener(listener);
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
    public void stopObserve(Observation observation, ObservationRegistryListener listener) {
        lwServer.getObservationRegistry().removeListener(listener);
        lwServer.getObservationRegistry().cancelObservation(observation);
    }

    /**
     * Stop all observations for the given client.
     *
     * @param client
     */
    public void stopObserve(Client client) {
        lwServer.getObservationRegistry().cancelObservations(client);
    }

    // The coap url /endpoint/objectId/instanceId maps to /bridgeID/thingTypeID/thingID
    public void requestChange(ObjectInstance id, String unit, ResourceModel resource, Command command)
            throws InterruptedException, TimeoutException {

        DownlinkRequest<?> request = null;

        int resourceID = resource.id;
        int objectID = id.getObjectID();
        int objectIDinstance = id.getInstanceID();

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

        LwM2mResponse cResponse = lwServer.send(id.getClient(), request, TIMEOUT);
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

    public ObjectModel getObjectModel(Client client, int objectID) {
        return lwServer.getModelProvider().getObjectModel(client).getObjectModel(objectID);
    }

    public Client getClient(String endpoint) {
        if ("demo".equals(endpoint)) {
            try {
                LinkObject[] objectLinks = { new LinkObject("/3200") };
                return new Client.Builder("clientIDDemo", "demo", InetAddress.getByName("127.0.0.1"),
                        LeshanServerBuilder.PORT, new InetSocketAddress(12345)).objectLinks(objectLinks).build();
            } catch (UnknownHostException ignored) {
            }
        }
        return lwServer.getClientRegistry().get(endpoint);
    }

    public LwM2mObjectInstance requestValues(ObjectInstance id) throws InterruptedException, CommunicationException {
        ReadRequest readRequest = new ReadRequest(id.getObjectID(), id.getInstanceID());
        ReadResponse readResponse = lwServer.send(id.getClient(), readRequest);
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
        discover.start(bundleContext, lwServer.getClientRegistry().allClients());
        lwServer.getClientRegistry().addListener(discover);
    }

    public void stopDiscovery() {
        discover.stop();
    }

    public ObjectInstance[] getObjectLinks(Client client) {
        LinkObject[] objectLinks = client.getObjectLinks();
        ObjectInstance[] objects = new ObjectInstance[objectLinks.length];
        for (int i = 0; i < objectLinks.length; i++) {
            LinkObject linkObject = objectLinks[i];
            objects[i] = new ObjectInstance(client, linkObject.getUrl());
        }
        return objects;
    }

    public void startClientObserve(ClientRegistryListener listener) {
        lwServer.getClientRegistry().addListener(listener);
    }

    public void stopClientObserve(ClientRegistryListener listener) {
        lwServer.getClientRegistry().removeListener(listener);
    }
}
