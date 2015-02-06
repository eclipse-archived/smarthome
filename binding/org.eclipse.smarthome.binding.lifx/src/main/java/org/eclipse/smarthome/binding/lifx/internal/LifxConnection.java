package org.eclipse.smarthome.binding.lifx.internal;

import java.util.List;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXFuzzyPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.light.LFXLightCollection.LFXLightCollectionListener;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;
import lifx.java.android.network_context.LFXNetworkContext.LFXNetworkContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import com.google.common.collect.Lists;

/**
 * {@link LifxConnection} is a helper class for the {@link LFXNetworkContext}.
 * It acts as singleton for common access to the gateway light connection and
 * contains a tracker functionality for all lights.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class LifxConnection implements LFXLightCollectionListener, LFXNetworkContextListener {

    public interface LifxLightTracker {
        void lightAdded(LFXLight light);

        void lightRemoved(LFXLight light);
    }

    private static LifxConnection lifxConnection = new LifxConnection();

    public static LifxConnection getInstance() {
        return lifxConnection;
    }

    private List<LFXLight> lights = Lists.newArrayList();

    private List<LifxLightTracker> lightTrackers = Lists.newArrayList();
    private LFXNetworkContext localNetworkContext;

    private Logger logger = LoggerFactory.getLogger(LifxConnection.class);

    public LifxConnection() {
        localNetworkContext = LFXClient.getSharedInstance(new Context()).getLocalNetworkContext();
    }

    public void addLightTracker(LifxLightTracker lightTracker) {
        synchronized (this) {
            for (LFXLight lfxLight : lights) {
                lightTracker.lightAdded(lfxLight);
            }
            lightTrackers.add(lightTracker);
        }
    }

    public void connect() {
        localNetworkContext.getAllLightsCollection().addLightCollectionListener(this);
        localNetworkContext.addNetworkContextListener(this);
        localNetworkContext.connect();
    }

    public void disconnect() {
        localNetworkContext.getAllLightsCollection().removeLightCollectionListener(this);
        localNetworkContext.removeNetworkContextListener(this);
        localNetworkContext.disconnect();
    }

    public LFXNetworkContext getNetworkContext() {
        return localNetworkContext;
    }

    @Override
    public void lightCollectionDidAddLight(LFXLightCollection arg0, LFXLight light) {
        synchronized (this) {
            lights.add(light);
            for (LifxLightTracker lightTracker : lightTrackers) {
                lightTracker.lightAdded(light);
            }
        }
    }

    @Override
    public void lightCollectionDidChangeColor(LFXLightCollection arg0, LFXHSBKColor arg1) {
        // nothing to do
    }

    @Override
    public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection arg0, LFXFuzzyPowerState arg1) {
        // nothing to do
    }

    @Override
    public void lightCollectionDidChangeLabel(LFXLightCollection arg0, String arg1) {
        // nothing to do
    }

    @Override
    public void lightCollectionDidRemoveLight(LFXLightCollection arg0, LFXLight light) {
        synchronized (this) {
            lights.remove(light);
            for (LifxLightTracker lightTracker : lightTrackers) {
                lightTracker.lightRemoved(light);
            }
        }
    }

    @Override
    public void networkContextDidAddTaggedLightCollection(LFXNetworkContext context,
            LFXTaggedLightCollection taggedLightCollection) {
        // nothing to do
    }

    @Override
    public void networkContextDidConnect(LFXNetworkContext context) {
        logger.debug("Connected to LIFX network.");
    }

    @Override
    public void networkContextDidDisconnect(LFXNetworkContext context) {
        logger.debug("Disconnected from LIFX network.");
    }

    @Override
    public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext context,
            LFXTaggedLightCollection taggedLightCollection) {
        // nothing to do
    }

    public void removeLightTracker(LifxLightTracker lightTracker) {
        synchronized (this) {
            for (LFXLight lfxLight : lights) {
                lightTracker.lightRemoved(lfxLight);
            }
            lightTrackers.remove(lightTracker);
        }
    }

}
